package com.yunbok.houseping.infrastructure.adapter.outbound.api;

import com.yunbok.houseping.domain.model.ApplyHomeSubscriptionInfo;
import com.yunbok.houseping.domain.model.HouseType;
import com.yunbok.houseping.domain.model.SubscriptionInfo;
import com.yunbok.houseping.domain.port.outbound.SubscriptionDataProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 청약Home API 어댑터
 * feature.subscription.applyhome-api-enabled=true 일 때만 활성화
 */
@Slf4j
@Component
@ConditionalOnProperty(
    name = "feature.subscription.applyhome-api-enabled",
    havingValue = "true",
    matchIfMissing = false
)
public class ApplyhomeApiAdapter implements SubscriptionDataProvider {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Value("${applyhome.api.key}")
    private String apiKey;

    private final WebClient webClient;

    public ApplyhomeApiAdapter(@Qualifier(value = "applyHomeWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public List<SubscriptionInfo> fetch(String areaName, LocalDate targetDate) {
        try {
            log.info("[청약Home API] {} 지역 데이터 수집 시작 (날짜: {})", areaName, targetDate);

            List<SubscriptionInfo> allSubscriptions = new ArrayList<>(fetchRegularApts(areaName, targetDate));
            allSubscriptions.addAll(fetchPrivatePreApts(areaName, targetDate));
            allSubscriptions.addAll(fetchNewlywedApts(areaName, targetDate));
            allSubscriptions.addAll(fetchRemainingApts(areaName, targetDate));

            log.info("[청약Home API] {} 지역에서 {}개 데이터 수집 완료", areaName, allSubscriptions.size());
            return allSubscriptions;

        } catch (Exception e) {
            log.error("[청약Home API] 데이터 수집 실패: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private List<ApplyHomeSubscriptionInfo> fetchRegularApts(String areaName, LocalDate targetDate) {
        return fetchAptSubscriptions(HouseType.APT, areaName, targetDate);
    }

    private List<ApplyHomeSubscriptionInfo> fetchPrivatePreApts(String areaName, LocalDate targetDate) {
        return fetchAptSubscriptions(HouseType.PRIVATE_PRE_SUBSCRIPTION, areaName, targetDate);
    }

    private List<ApplyHomeSubscriptionInfo> fetchNewlywedApts(String areaName, LocalDate targetDate) {
        return fetchAptSubscriptions(HouseType.NEWLYWED_TOWN, areaName, targetDate);
    }

    private List<ApplyHomeSubscriptionInfo> fetchRemainingApts(String areaName, LocalDate targetDate) {
        return fetchRemainingAptSubscriptions(areaName, targetDate);
    }

    /**
     * 일반 APT API 호출
     */
    private List<ApplyHomeSubscriptionInfo> fetchAptSubscriptions(HouseType houseType, String areaName, LocalDate targetDate) {
        Map<String, Object> response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/getAPTLttotPblancDetail")
                        .queryParam("page", 1)
                        .queryParam("perPage", 5000)
                        .queryParam("cond[HOUSE_SECD::EQ]", houseType.getHouseSecd())
                        .queryParam("cond[SUBSCRPT_AREA_CODE_NM::EQ]", areaName)
                        .queryParam("serviceKey", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return parseAptResponse(response, houseType.getDisplayName(), targetDate);
    }

    /**
     * 잔여세대 APT API 호출
     */
    private List<ApplyHomeSubscriptionInfo> fetchRemainingAptSubscriptions(String areaName, LocalDate targetDate) {
        Map<String, Object> response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/getRemndrLttotPblancDetail")
                        .queryParam("page", 1)
                        .queryParam("perPage", 5000)
                        .queryParam("cond[SUBSCRPT_AREA_CODE_NM::EQ]", areaName)
                        .queryParam("serviceKey", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return parseRemainingResponse(response, targetDate);
    }

    /**
     * 일반 APT API 응답 파싱
     */
    @SuppressWarnings("unchecked")
    private List<ApplyHomeSubscriptionInfo> parseAptResponse(Map<String, Object> response, String houseType, LocalDate targetDate) {
        if (response == null) return Collections.emptyList();

        List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
        if (data == null) return Collections.emptyList();

        return data.stream()
                .filter(item -> isReceiptDateInRange((String) item.get("RCEPT_BGNDE"), targetDate))
                .map(item -> buildSubscriptionInfo(item, houseType))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    /**
     * 잔여세대 API 응답 파싱
     */
    @SuppressWarnings("unchecked")
    private List<ApplyHomeSubscriptionInfo> parseRemainingResponse(Map<String, Object> response, LocalDate targetDate) {
        if (response == null) return Collections.emptyList();

        List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
        if (data == null) return Collections.emptyList();

        return data.stream()
                .filter(item -> isReceiptDateInRange((String) item.get("SUBSCRPT_RCEPT_BGNDE"), targetDate))
                .map(this::buildRemainingSubscriptionInfo)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private boolean isReceiptDateInRange(String dateStr, LocalDate targetDate) {
        if (dateStr == null || dateStr.isEmpty()) {
            return false;
        }

        try {
            LocalDate date = LocalDate.parse(dateStr, DATE_FORMATTER);
            return date.equals(targetDate);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 일반 APT SubscriptionInfo 생성
     */
    private Optional<ApplyHomeSubscriptionInfo> buildSubscriptionInfo(Map<String, Object> item, String houseType) {
        try {
            return Optional.of(ApplyHomeSubscriptionInfo.builder()
                    .houseManageNo(getString(item, "HOUSE_MANAGE_NO"))
                    .pblancNo(getString(item, "PBLANC_NO"))
                    .houseName(getString(item, "HOUSE_NM"))
                    .houseType(houseType)
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
            log.warn("SubscriptionInfo 생성 실패: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 잔여세대 SubscriptionInfo 생성
     */
    private Optional<ApplyHomeSubscriptionInfo> buildRemainingSubscriptionInfo(Map<String, Object> item) {
        try {
            return Optional.of(ApplyHomeSubscriptionInfo.builder()
                    .houseManageNo(getString(item, "HOUSE_MANAGE_NO"))
                    .pblancNo(getString(item, "PBLANC_NO"))
                    .houseName(getString(item, "HOUSE_NM"))
                    .houseType(HouseType.REMAINING.getDisplayName())
                    .area(getString(item, "SUBSCRPT_AREA_CODE_NM"))
                    .announceDate(parseDate(getString(item, "RCRIT_PBLANC_DE")))
                    .receiptStartDate(parseDate(getString(item, "SUBSCRPT_RCEPT_BGNDE")))
                    .receiptEndDate(parseDate(getString(item, "SUBSCRPT_RCEPT_ENDDE")))
                    .winnerAnnounceDate(parseDate(getString(item, "PRZWNER_PRESNATN_DE")))
                    .homepageUrl(getString(item, "HMPG_ADRES"))
                    .detailUrl(getString(item, "PBLANC_URL"))
                    .contact(getString(item, "MDHS_TELNO"))
                    .totalSupplyCount(getInteger(item, "TOT_SUPLY_HSHLDCO"))
                    .build());
        } catch (Exception e) {
            log.warn("잔여세대 SubscriptionInfo 생성 실패: {}", e.getMessage());
            return Optional.empty();
        }
    }

    // 유틸리티 메서드들
    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? String.valueOf(value) : "";
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
