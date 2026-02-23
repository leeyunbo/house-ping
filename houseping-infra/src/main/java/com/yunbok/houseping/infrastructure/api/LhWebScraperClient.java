package com.yunbok.houseping.infrastructure.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yunbok.houseping.core.domain.Subscription;
import com.yunbok.houseping.infrastructure.dto.LhSubscriptionInfo;
import com.yunbok.houseping.core.domain.SubscriptionSource;
import com.yunbok.houseping.core.port.SubscriptionProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.yunbok.houseping.support.util.DateParsingUtil;

import static com.yunbok.houseping.support.util.MapExtractor.getString;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * LH 웹 캘린더 API 어댑터 (API 다운 시 대체 소스)
 * 엔드포인트: /lhapply/apply/sc/detail.do
 * feature.subscription.lh-web-enabled=true 일 때만 활성화
 */
@Slf4j
@Component
@ConditionalOnProperty(
    name = "feature.subscription.lh-web-enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class LhWebScraperClient implements SubscriptionProvider {

    private static final DateTimeFormatter LH_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String LH_PROVIDER_NAME = SubscriptionSource.LH.getValue() + "웹";
    private static final String SOURCE_NAME = SubscriptionSource.LH.getValue();

    /** 비주거용 공고 제외 키워드 */
    private static final Set<String> NON_RESIDENTIAL_KEYWORDS = Set.of(
            "상가", "어린이집", "가스충전소", "용지", "입점자", "임차운영자"
    );

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public LhWebScraperClient(@Qualifier("lhWebCalendarClient") WebClient webClient) {
        this.webClient = webClient;
        this.objectMapper = new ObjectMapper();
    }

    public String getSourceName() {
        return SOURCE_NAME;
    }

    public List<Subscription> fetch(String areaName, LocalDate targetDate) {
        try {
            log.info("[{}] {} 지역 데이터 수집 시작 (날짜: {})", LH_PROVIDER_NAME, areaName, targetDate);

            // targetDate를 YYYYMMDD 형식으로 변환
            String panDt = targetDate.format(LH_DATE_FORMATTER);
            String selectYear = String.valueOf(targetDate.getYear());
            String selectMonth = String.format("%02d", targetDate.getMonthValue());

            // POST 요청 파라미터 구성
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("panDt", panDt);
            formData.add("selectYear", selectYear);
            formData.add("selectMonth", selectMonth);

            // detail.do 엔드포인트로 해당 날짜 관련 공고 조회
            String responseStr = webClient.post()
                    .uri("/lhapply/apply/sc/detail.do")
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("[{}] 분양+임대 응답 수신: {}", LH_PROVIDER_NAME, responseStr);

            Map<String, Object> response = objectMapper.readValue(responseStr, Map.class);
            List<Subscription> result = parseDetailResponse(response, areaName, targetDate);

            log.info("[{}] {} 지역에서 {}개 데이터 수집 완료", LH_PROVIDER_NAME, areaName, result.size());

            return result;

        } catch (Exception e) {
            log.error("[{}] 데이터 수집 실패: {}", LH_PROVIDER_NAME, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * detail.do API 응답 파싱
     */
    @SuppressWarnings("unchecked")
    private List<Subscription> parseDetailResponse(Map<String, Object> response, String areaName, LocalDate targetDate) {
        if (response == null) {
            log.info("[{}] 응답이 null입니다.", LH_PROVIDER_NAME);
            return Collections.emptyList();
        }

        List<Map<String, Object>> panList = (List<Map<String, Object>>) response.get("panList");
        if (panList == null || panList.isEmpty()) {
            log.info("[{}] panList가 비어있습니다.", LH_PROVIDER_NAME);
            return Collections.emptyList();
        }

        return panList.stream()
                .filter(item -> isReceiptStartDate(item, targetDate))
                .filter(item -> matchesArea(item, areaName))
                .filter(this::isResidential)
                .map(this::buildLhSubscriptionInfo)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(LhSubscriptionInfo::toSubscription)
                .collect(Collectors.toList());
    }

    /**
     * 접수 시작일이 targetDate와 일치하는지 확인
     */
    private boolean isReceiptStartDate(Map<String, Object> item, LocalDate targetDate) {
        String acpStDttm = getString(item, "acpStDttm");
        if (acpStDttm == null || acpStDttm.isEmpty()) {
            return false;
        }

        LocalDate startDate = DateParsingUtil.parse(acpStDttm);
        return startDate != null && startDate.equals(targetDate);
    }

    /**
     * 지역 필터링 (지역명이 일치하거나 포함되는지 확인)
     */
    private boolean matchesArea(Map<String, Object> item, String areaName) {
        String cnpCdNm = getString(item, "cnpCdNm");

        // 지역 정보가 없으면 제외 (정확한 데이터만 수집)
        if (cnpCdNm == null || cnpCdNm.isEmpty()) {
            log.debug("[{}] 지역 정보 없는 공고 제외: {}", LH_PROVIDER_NAME, getString(item, "panNm"));
            return false;
        }

        // 지역 정보가 있으면 서울/경기만 필터링
        return cnpCdNm.contains(areaName);
    }

    /**
     * 주거용 공고인지 확인 (상가, 어린이집 등 비주거용 제외)
     */
    private boolean isResidential(Map<String, Object> item) {
        String panNm = getString(item, "panNm");
        if (panNm == null || panNm.isEmpty()) {
            return false;
        }

        boolean isNonResidential = NON_RESIDENTIAL_KEYWORDS.stream()
                .anyMatch(panNm::contains);

        if (isNonResidential) {
            log.debug("[{}] 비주거용 공고 제외: {}", LH_PROVIDER_NAME, panNm);
        }

        return !isNonResidential;
    }

    /**
     * LH 데이터를 SubscriptionInfo로 변환
     */
    private Optional<LhSubscriptionInfo> buildLhSubscriptionInfo(Map<String, Object> item) {
        try {
            String panNm = getString(item, "panNm");
            String cnpCdNm = getString(item, "cnpCdNm");
            String houseType = determineHouseType(item);

            return Optional.of(LhSubscriptionInfo.builder()
                    .houseName(panNm)
                    .houseType(houseType)
                    .area(cnpCdNm != null ? cnpCdNm : "전국/미분류")
                    .announceDate(DateParsingUtil.parse(getString(item, "panNtStDt")))
                    .receiptStartDate(DateParsingUtil.parse(getString(item, "acpStDttm")))
                    .receiptEndDate(DateParsingUtil.parse(getString(item, "acpEdDttm")))
                    .detailUrl(getString(item, "dtlUrl"))
                    .subscriptionStatus(getString(item, "panSs"))
                    .build());
        } catch (Exception e) {
            log.warn("[{}] SubscriptionInfo 생성 실패: {}", LH_PROVIDER_NAME, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 응답 데이터에서 주택 유형 결정
     */
    private String determineHouseType(Map<String, Object> item) {
        String uppAisTpCd = getString(item, "uppAisTpCd");
        if (uppAisTpCd == null) {
            return "LH 주택";
        }

        return switch (uppAisTpCd) {
            case "05" -> "LH 분양주택";
            case "06" -> "LH 임대주택";
            case "39" -> "LH 신혼희망타운";
            default -> "LH 주택";
        };
    }

    public List<Subscription> fetchAll(String areaName) {
        // LH는 "접수중" 상태만 조회하므로 fetch와 동일
        return fetch(areaName, LocalDate.now());
    }
}
