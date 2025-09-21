package com.yunbok.houseping.infrastructure.adapter.outbound.api;

import com.yunbok.houseping.domain.model.ApplyHomeSubscriptionInfo;
import com.yunbok.houseping.domain.model.SubscriptionInfo;
import com.yunbok.houseping.domain.port.outbound.SubscriptionDataProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 청약Home API 어댑터
 */
@Slf4j
@Component
public class ApplyhomeApiAdapter implements SubscriptionDataProvider {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Value("${applyhome.api.key}")
    private String apiKey;

    private final WebClient webClient;

    public ApplyhomeApiAdapter(@Qualifier(value = "applyHomeWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public List<SubscriptionInfo> fetch(String areaCode, LocalDate targetDate) {
        List<SubscriptionInfo> allSubscriptions = new ArrayList<>(fetchRegularApts(areaCode, targetDate));
        allSubscriptions.addAll(fetchPrivatePreApts(areaCode, targetDate));
        allSubscriptions.addAll(fetchNewlywedApts(areaCode, targetDate));
        allSubscriptions.addAll(fetchRemainingApts(areaCode, targetDate));
        return allSubscriptions;
    }

    private List<ApplyHomeSubscriptionInfo> fetchRegularApts(String areaCode, LocalDate targetDate) {
        return fetchAptSubscriptions("01", areaCode, targetDate);
    }

    private List<ApplyHomeSubscriptionInfo> fetchPrivatePreApts(String areaCode, LocalDate targetDate) {
        return fetchAptSubscriptions("09", areaCode, targetDate);
    }

    private List<ApplyHomeSubscriptionInfo> fetchNewlywedApts(String areaCode, LocalDate targetDate) {
        return fetchAptSubscriptions("10", areaCode, targetDate);
    }

    private List<ApplyHomeSubscriptionInfo> fetchRemainingApts(String areaCode, LocalDate targetDate) {
        return fetchRemainingAptSubscriptions(areaCode, targetDate);
    }

    /**
     * 일반 APT API 호출
     */
    private List<ApplyHomeSubscriptionInfo> fetchAptSubscriptions(String houseSecd, String areaCode, LocalDate targetDate) {
        String dateStr = targetDate.format(DATE_FORMATTER);

        Map<String, Object> response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/getAPTLttotPblancDetail")
                        .queryParam("page", 1)
                        .queryParam("perPage", 100)
                        .queryParam("cond[HOUSE_SECD::EQ]", houseSecd)
                        .queryParam("cond[SUBSCRPT_AREA_CODE::EQ]", areaCode)
                        .queryParam("cond[RCRIT_PBLANC_DE::EQ]", targetDate)
                        .queryParam("serviceKey", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        log.info("청약홈 API 응답 (houseSecd: {}, areaCode: {}, date: {}): {}", houseSecd, areaCode, dateStr, response);

        return parseAptResponse(response, getHouseTypeName(houseSecd));
    }

    /**
     * 잔여세대 APT API 호출
     */
    private List<ApplyHomeSubscriptionInfo> fetchRemainingAptSubscriptions(String areaCode, LocalDate targetDate) {
        String dateStr = targetDate.format(DATE_FORMATTER);

        Map<String, Object> response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/getRemndrLttotPblancDetail")
                        .queryParam("page", 1)
                        .queryParam("perPage", 100)
                        .queryParam("cond[HOUSE_SECD::EQ]", "04") // 무순위
                        .queryParam("cond[SUBSCRPT_AREA_CODE::EQ]", areaCode)
                        .queryParam("cond[RCRIT_PBLANC_DE::GTE]", dateStr)
                        .queryParam("cond[RCRIT_PBLANC_DE::LTE]", dateStr)
                        .queryParam("serviceKey", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return parseRemainingResponse(response);
    }

    /**
     * 일반 APT API 응답 파싱
     */
    @SuppressWarnings("unchecked")
    private List<ApplyHomeSubscriptionInfo> parseAptResponse(Map<String, Object> response, String houseType) {
        if (response == null) return Collections.emptyList();

        List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
        if (data == null) return Collections.emptyList();

        return data.stream()
                .map(item -> buildSubscriptionInfo(item, houseType))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    /**
     * 잔여세대 API 응답 파싱
     */
    @SuppressWarnings("unchecked")
    private List<ApplyHomeSubscriptionInfo> parseRemainingResponse(Map<String, Object> response) {
        if (response == null) return Collections.emptyList();

        List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
        if (data == null) return Collections.emptyList();

        return data.stream()
                .map(item -> buildRemainingSubscriptionInfo(item))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
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
                    .houseType("무순위")
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

    private String getHouseTypeName(String houseSecd) {
        return switch (houseSecd) {
            case "01" -> "APT";
            case "09" -> "민간사전청약";
            case "10" -> "신혼희망타운";
            default -> "기타";
        };
    }
}
