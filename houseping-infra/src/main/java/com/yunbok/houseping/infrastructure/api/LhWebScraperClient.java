package com.yunbok.houseping.infrastructure.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yunbok.houseping.core.domain.Subscription;
import com.yunbok.houseping.infrastructure.dto.LhSubscriptionInfo;
import com.yunbok.houseping.infrastructure.support.LhResidentialFilter;
import com.yunbok.houseping.core.domain.SubscriptionSource;
import com.yunbok.houseping.core.port.SubscriptionProvider;
import com.yunbok.houseping.support.external.LhWebCalendarItem;
import com.yunbok.houseping.support.external.LhWebCalendarResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.yunbok.houseping.support.util.DateParsingUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public LhWebScraperClient(
            @Qualifier("lhWebCalendarClient") WebClient webClient,
            ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    public String getSourceName() {
        return SOURCE_NAME;
    }

    public List<Subscription> fetch(String areaName, LocalDate targetDate) {
        try {
            log.info("[{}] {} 지역 데이터 수집 시작 (날짜: {})", LH_PROVIDER_NAME, areaName, targetDate);

            String panDt = targetDate.format(LH_DATE_FORMATTER);
            String selectYear = String.valueOf(targetDate.getYear());
            String selectMonth = String.format("%02d", targetDate.getMonthValue());

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("panDt", panDt);
            formData.add("selectYear", selectYear);
            formData.add("selectMonth", selectMonth);

            String responseStr = webClient.post()
                    .uri("/lhapply/apply/sc/detail.do")
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("[{}] 분양+임대 응답 수신: {}", LH_PROVIDER_NAME, responseStr);

            LhWebCalendarResponse response = objectMapper.readValue(responseStr, LhWebCalendarResponse.class);
            List<Subscription> result = parseDetailResponse(response, areaName, targetDate);

            log.info("[{}] {} 지역에서 {}개 데이터 수집 완료", LH_PROVIDER_NAME, areaName, result.size());
            return result;

        } catch (Exception e) {
            log.error("[{}] 데이터 수집 실패: {}", LH_PROVIDER_NAME, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private List<Subscription> parseDetailResponse(LhWebCalendarResponse response, String areaName, LocalDate targetDate) {
        if (response == null) {
            log.info("[{}] 응답이 null입니다.", LH_PROVIDER_NAME);
            return Collections.emptyList();
        }

        List<LhWebCalendarItem> items = response.getItems();
        if (items.isEmpty()) {
            log.info("[{}] panList가 비어있습니다.", LH_PROVIDER_NAME);
            return Collections.emptyList();
        }

        return items.stream()
                .filter(item -> isReceiptStartDate(item, targetDate))
                .filter(item -> matchesArea(item, areaName))
                .filter(item -> LhResidentialFilter.isResidential(item.projectName()))
                .map(this::buildLhSubscriptionInfo)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(LhSubscriptionInfo::toSubscription)
                .toList();
    }

    private boolean isReceiptStartDate(LhWebCalendarItem item, LocalDate targetDate) {
        String receiptStart = item.receiptStartDate();
        if (receiptStart == null || receiptStart.isEmpty()) {
            return false;
        }
        LocalDate startDate = DateParsingUtil.parse(receiptStart);
        return startDate != null && startDate.equals(targetDate);
    }

    private boolean matchesArea(LhWebCalendarItem item, String areaName) {
        String area = item.areaName();
        if (area == null || area.isEmpty()) {
            log.debug("[{}] 지역 정보 없는 공고 제외: {}", LH_PROVIDER_NAME, item.projectName());
            return false;
        }
        return area.contains(areaName);
    }

    private Optional<LhSubscriptionInfo> buildLhSubscriptionInfo(LhWebCalendarItem item) {
        try {
            return Optional.of(LhSubscriptionInfo.builder()
                    .houseName(item.projectName())
                    .houseType(determineHouseType(item.houseTypeCode()))
                    .area(item.areaName() != null ? item.areaName() : "전국/미분류")
                    .announceDate(DateParsingUtil.parse(item.announceDate()))
                    .receiptStartDate(DateParsingUtil.parse(item.receiptStartDate()))
                    .receiptEndDate(DateParsingUtil.parse(item.receiptEndDate()))
                    .detailUrl(item.detailUrl())
                    .subscriptionStatus(item.status())
                    .build());
        } catch (Exception e) {
            log.warn("[{}] SubscriptionInfo 생성 실패: {}", LH_PROVIDER_NAME, e.getMessage());
            return Optional.empty();
        }
    }

    private String determineHouseType(String uppAisTpCd) {
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
        return fetch(areaName, LocalDate.now());
    }
}
