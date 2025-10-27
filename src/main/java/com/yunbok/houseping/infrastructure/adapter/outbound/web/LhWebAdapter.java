package com.yunbok.houseping.infrastructure.adapter.outbound.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yunbok.houseping.domain.model.LhSubscriptionInfo;
import com.yunbok.houseping.domain.model.SubscriptionInfo;
import com.yunbok.houseping.domain.port.outbound.SubscriptionOuterWorldProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
    matchIfMissing = true  // 설정이 없으면 기본 활성화
)
public class LhWebAdapter implements SubscriptionOuterWorldProvider {

    private static final DateTimeFormatter LH_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter LH_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final String LH_PROVIDER_NAME = "LH웹";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public LhWebAdapter(@Qualifier("lhWebCalendarClient") WebClient webClient) {
        this.webClient = webClient;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<SubscriptionInfo> fetch(String areaName, LocalDate targetDate) {
        try {
            log.info("[{}] {} 지역 데이터 수집 시작 (날짜: {})", LH_PROVIDER_NAME, areaName, targetDate);

            // targetDate를 YYYYMMDD 형식으로 변환
            String panDt = targetDate.format(LH_DATE_FORMATTER);
            String selectYear = String.valueOf(targetDate.getYear());
            String selectMonth = String.format("%02d", targetDate.getMonthValue());

            // POST 요청 파라미터 구성 (분양주택만 조회)
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("panDt", panDt);
            formData.add("selectYear", selectYear);
            formData.add("selectMonth", selectMonth);
            formData.add("calSrchType", "02"); // 02=분양 (임대 제외)

            // detail.do 엔드포인트로 JSON 데이터 요청
            String responseStr = webClient.post()
                    .uri("/lhapply/apply/sc/detail.do")
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("[{}] 분양주택 응답 수신: {}", LH_PROVIDER_NAME, responseStr);

            Map<String, Object> response = objectMapper.readValue(responseStr, Map.class);
            List<SubscriptionInfo> saleResult = parseDetailResponse(response, areaName, targetDate, "분양주택");

            log.info("[{}] {} 지역에서 {}개 데이터 수집 완료", LH_PROVIDER_NAME, areaName, saleResult.size());

            return saleResult;

        } catch (Exception e) {
            log.error("[{}] 데이터 수집 실패: {}", LH_PROVIDER_NAME, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * detail.do API 응답 파싱
     */
    @SuppressWarnings("unchecked")
    private List<SubscriptionInfo> parseDetailResponse(Map<String, Object> response, String areaName, LocalDate targetDate, String houseType) {
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
                .map(item -> buildLhSubscriptionInfo(item, houseType))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .<SubscriptionInfo>map(info -> info)
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

        try {
            // parseLhDate를 사용하여 다양한 날짜 형식 처리
            LocalDate startDate = parseLhDate(acpStDttm);
            return startDate != null && startDate.equals(targetDate);
        } catch (Exception e) {
            log.warn("[{}] 접수시작일시 파싱 실패: {}", LH_PROVIDER_NAME, acpStDttm);
            return false;
        }
    }

    /**
     * 지역 필터링 (지역명이 일치하거나 포함되는지 확인)
     */
    private boolean matchesArea(Map<String, Object> item, String areaName) {
        String cnpCdNm = getString(item, "cnpCdNm");

        // 지역 정보가 없으면 포함 (전국/미분류로 표시됨)
        if (cnpCdNm == null || cnpCdNm.isEmpty()) {
            return true;
        }

        // 지역 정보가 있으면 서울/경기만 필터링
        return cnpCdNm.contains(areaName);
    }

    /**
     * LH 데이터를 SubscriptionInfo로 변환
     */
    private Optional<LhSubscriptionInfo> buildLhSubscriptionInfo(Map<String, Object> item, String houseType) {
        try {
            String panNm = getString(item, "panNm");
            String cnpCdNm = getString(item, "cnpCdNm");

            return Optional.of(LhSubscriptionInfo.builder()
                    .houseName(panNm + " [" + LH_PROVIDER_NAME + "]")
                    .houseType(houseType)
                    .area(cnpCdNm != null ? cnpCdNm : "전국/미분류")
                    .announceDate(parseLhDate(getString(item, "panNtStDt")))
                    .receiptEndDate(parseLhDate(getString(item, "acpEdDttm")))
                    .detailUrl(getString(item, "dtlUrl"))
                    .subscriptionStatus(getString(item, "panSs"))
                    .build());
        } catch (Exception e) {
            log.warn("[{}] SubscriptionInfo 생성 실패: {}", LH_PROVIDER_NAME, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * LH 날짜/날짜시간 파싱
     */
    private LocalDate parseLhDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty() || "-".equals(dateStr)) {
            return null;
        }

        try {
            String cleanDateStr = dateStr.trim();

            // YYYYMMDDHHmm 형식 (12자리)
            if (cleanDateStr.length() == 12 && cleanDateStr.matches("\\d{12}")) {
                LocalDateTime dateTime = LocalDateTime.parse(cleanDateStr, LH_DATETIME_FORMATTER);
                return dateTime.toLocalDate();
            }
            // YYYYMMDD 형식 (8자리)
            else if (cleanDateStr.length() == 8 && cleanDateStr.matches("\\d{8}")) {
                return LocalDate.parse(cleanDateStr, LH_DATE_FORMATTER);
            }
            // yyyy.MM.dd 형식
            else if (cleanDateStr.contains(".")) {
                return LocalDate.parse(cleanDateStr, DISPLAY_DATE_FORMATTER);
            }
            // yyyy-MM-dd 형식
            else if (cleanDateStr.contains("-")) {
                return LocalDate.parse(cleanDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }

            log.warn("[{}] 지원하지 않는 날짜 형식: {}", LH_PROVIDER_NAME, dateStr);
            return null;

        } catch (Exception e) {
            log.warn("[{}] 날짜 파싱 실패: {}", LH_PROVIDER_NAME, dateStr);
            return null;
        }
    }

    @Override
    public List<SubscriptionInfo> fetchAll(String areaName) {
        // LH는 "접수중" 상태만 조회하므로 fetch와 동일
        return fetch(areaName, LocalDate.now());
    }

    /**
     * Map에서 안전하게 String 값 추출
     */
    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        String str = String.valueOf(value).trim();
        return str.isEmpty() || "-".equals(str) ? null : str;
    }
}
