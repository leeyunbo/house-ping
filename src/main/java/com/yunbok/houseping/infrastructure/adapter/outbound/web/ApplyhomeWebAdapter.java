package com.yunbok.houseping.infrastructure.adapter.outbound.web;

import com.yunbok.houseping.domain.model.ApplyHomeSubscriptionInfo;
import com.yunbok.houseping.domain.model.SubscriptionInfo;
import com.yunbok.houseping.domain.port.outbound.SubscriptionDataProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 청약Home 웹 캘린더 API 어댑터 (API 다운 시 대체 소스)
 * 엔드포인트: /ai/aib/selectSubscrptCalender.do
 * feature.subscription.applyhome-web-enabled=true 일 때만 활성화
 */
@Slf4j
@Component
@ConditionalOnProperty(
    name = "feature.subscription.applyhome-web-enabled",
    havingValue = "true",
    matchIfMissing = true  // 설정이 없으면 기본 활성화
)
public class ApplyhomeWebAdapter implements SubscriptionDataProvider {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final WebClient webClient;

    public ApplyhomeWebAdapter(@Qualifier("applyHomeWebCalendarClient") WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public List<SubscriptionInfo> fetch(String areaName, LocalDate targetDate) {
        try {
            log.info("[청약Home 웹] {} 지역 데이터 수집 시작 (날짜: {})", areaName, targetDate);

            String yearMonth = targetDate.format(MONTH_FORMATTER);

            Map<String, Object> response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/ai/aib/selectSubscrptCalender.do")
                            .queryParam("yearMonth", yearMonth)
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            log.info(response.toString());

            List<SubscriptionInfo> result = parseCalendarResponse(response, areaName, targetDate);
            log.info("[청약Home 웹] {} 지역에서 {}개 데이터 수집 완료", areaName, result.size());

            return result;

        } catch (Exception e) {
            log.error("[청약Home 웹] 데이터 수집 실패: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 캘린더 API 응답 파싱
     */
    @SuppressWarnings("unchecked")
    private List<SubscriptionInfo> parseCalendarResponse(Map<String, Object> response, String areaName, LocalDate targetDate) {
        if (response == null) {
            log.warn("[청약Home 웹] 응답이 null입니다.");
            return Collections.emptyList();
        }

        List<Map<String, Object>> schdulList = (List<Map<String, Object>>) response.get("schdulList");
        if (schdulList == null || schdulList.isEmpty()) {
            log.info("[청약Home 웹] schdulList가 비어있습니다.");
            return Collections.emptyList();
        }

        return schdulList.stream()
                .filter(item -> matchesAreaAndDate(item, areaName, targetDate))
                .map(this::buildSubscriptionInfo)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .<SubscriptionInfo>map(info -> info)
                .toList();
    }

    /**
     * 지역과 날짜 필터링
     */
    private boolean matchesAreaAndDate(Map<String, Object> item, String areaName, LocalDate targetDate) {
        // 지역 필터링
        String itemArea = getString(item, "SUBSCRPT_AREA_CODE_NM");
        if (itemArea == null || !itemArea.equals(areaName)) {
            return false;
        }

        // 날짜 필터링 (IN_DAY가 접수시작일)
        String inDay = getString(item, "IN_DAY");
        if (inDay == null) {
            return false;
        }

        try {
            LocalDate receiptStartDate = LocalDate.parse(inDay, DATE_FORMATTER);
            return receiptStartDate.equals(targetDate);
        } catch (Exception e) {
            log.warn("[청약Home 웹] 날짜 파싱 실패: {}", inDay);
            return false;
        }
    }

    /**
     * SubscriptionInfo 생성
     */
    private Optional<ApplyHomeSubscriptionInfo> buildSubscriptionInfo(Map<String, Object> item) {
        try {
            return Optional.of(ApplyHomeSubscriptionInfo.builder()
                    .houseManageNo(getString(item, "HOUSE_MANAGE_NO"))
                    .pblancNo(getString(item, "PBLANC_NO"))
                    .houseName(getString(item, "HOUSE_NM"))
                    .houseType(getString(item, "RCEPT_SE")) // 접수구분 (일반공급, 무순위 등)
                    .area(getString(item, "SUBSCRPT_AREA_CODE_NM"))
                    .announceDate(parseDate(getString(item, "RCRIT_PBLANC_DE")))
                    .receiptStartDate(parseDate(getString(item, "RCEPT_BGNDE")))
                    .receiptEndDate(parseDate(getString(item, "RCEPT_ENDDE")))
                    .winnerAnnounceDate(parseDate(getString(item, "PRZWNER_PRESNATN_DE")))
                    .homepageUrl(getString(item, "HMPG_ADRES"))
                    .detailUrl(getString(item, "PBLANC_URL"))
                    .contact(getString(item, "MDHS_TELNO"))
                    .totalSupplyCount(getInteger(item, "TOT_SUPLY_HSHLDCO"))
                    .build());
        } catch (Exception e) {
            log.warn("[청약Home 웹] SubscriptionInfo 생성 실패: {}", e.getMessage());
            return Optional.empty();
        }
    }

    // 유틸리티 메서드들
    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? String.valueOf(value).trim() : "";
    }

    private Integer getInteger(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty() || dateStr.equals("-")) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }
}
