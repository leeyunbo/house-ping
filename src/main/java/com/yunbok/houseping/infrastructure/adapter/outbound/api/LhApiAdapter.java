package com.yunbok.houseping.infrastructure.adapter.outbound.api;

import com.yunbok.houseping.domain.model.AreaCodeMapping;
import com.yunbok.houseping.domain.model.LhApiTypeCode;
import com.yunbok.houseping.domain.model.LhSubscriptionInfo;
import com.yunbok.houseping.domain.model.SubscriptionInfo;
import com.yunbok.houseping.domain.port.outbound.SubscriptionOuterWorldProvider;
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
 * LH(한국토지주택공사) API 어댑터
 * feature.subscription.lh-api-enabled=true 일 때만 활성화
 */
@Slf4j
@Component
@ConditionalOnProperty(
        name = "feature.subscription.lh-api-enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class LhApiAdapter implements SubscriptionOuterWorldProvider {

    private static final DateTimeFormatter LH_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String LH_PROVIDER_NAME = "LH";

    @Value("${lh.api.key}")
    private String apiKey;

    private final WebClient webClient;

    public LhApiAdapter(@Qualifier("lhWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public List<SubscriptionInfo> fetch(String areaName, LocalDate targetDate) {
        log.info("[LH API] {} 지역 데이터 수집 시작 (날짜: {})", areaName, targetDate);

        // 분양주택과 신혼희망타운만 조회 (임대주택 제외)
        List<SubscriptionInfo> allSubscriptions = new ArrayList<>(fetchRegularApts(areaName));
        allSubscriptions.addAll(fetchNewlywedApts(areaName));

        log.info("[LH API] {} 지역에서 {}개 데이터 수집 완료", areaName, allSubscriptions.size());
        return allSubscriptions;

    }

    @Override
    public List<SubscriptionInfo> fetchAll(String areaName) {
        try {
            log.info("[LH API] {} 지역 전체 데이터 수집 시작 (DB 동기화용)", areaName);

            // 분양주택과 신혼희망타운만 조회 (임대주택 제외)
            List<SubscriptionInfo> allSubscriptions = new ArrayList<>(fetchAllRegularApts(areaName));
            allSubscriptions.addAll(fetchAllNewlywedApts(areaName));

            log.info("[LH API] {} 지역에서 총 {}개 데이터 수집 완료", areaName, allSubscriptions.size());
            return allSubscriptions;

        } catch (Exception e) {
            log.error("[LH API] 전체 데이터 수집 실패: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private List<LhSubscriptionInfo> fetchRegularApts(String areaName) {
        return fetchLhSubscriptions(LhApiTypeCode.SALE_APT, areaName);
    }

    private List<LhSubscriptionInfo> fetchNewlywedApts(String areaName) {
        return fetchLhSubscriptions(LhApiTypeCode.NEWLYWED_APT, areaName);
    }

    private List<LhSubscriptionInfo> fetchAllRegularApts(String areaName) {
        return fetchAllLhSubscriptions(LhApiTypeCode.SALE_APT, areaName);
    }

    private List<LhSubscriptionInfo> fetchAllNewlywedApts(String areaName) {
        return fetchAllLhSubscriptions(LhApiTypeCode.NEWLYWED_APT, areaName);
    }

    /**
     * LH API 공통 호출 메서드 (특정 날짜)
     */
    private List<LhSubscriptionInfo> fetchLhSubscriptions(LhApiTypeCode apiTypeCode, String areaName) {
        String lhAreaCode = AreaCodeMapping.getLhAreaCodeByName(areaName);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("serviceKey", apiKey)
                        .queryParam("PG_SZ", 5000)
                        .queryParam("PAGE", 1)
                        .queryParam("UPP_AIS_TP_CD", apiTypeCode.getTypeCode())
                        .queryParam("PAN_SS", "접수중")
                        .queryParam("CNP_CD", lhAreaCode)
                        .build())
                .retrieve()
                .bodyToMono(List.class)
                .block();

        return parseLhResponse(response, apiTypeCode.getDisplayName());
    }

    /**
     * LH API 공통 호출 메서드 (전체 데이터)
     */
    private List<LhSubscriptionInfo> fetchAllLhSubscriptions(LhApiTypeCode apiTypeCode, String areaName) {
        String lhAreaCode = AreaCodeMapping.getLhAreaCodeByName(areaName);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("serviceKey", apiKey)
                        .queryParam("PG_SZ", 5000)
                        .queryParam("PAGE", 1)
                        .queryParam("UPP_AIS_TP_CD", apiTypeCode.getTypeCode())
                        .queryParam("CNP_CD", lhAreaCode)
                        .build())
                .retrieve()
                .bodyToMono(List.class)
                .block();

        return parseAllLhResponse(response, apiTypeCode.getDisplayName());
    }

    /**
     * LH API 응답 파싱 및 필터링 (특정 날짜)
     */
    @SuppressWarnings("unchecked")
    private List<LhSubscriptionInfo> parseLhResponse(List<Map<String, Object>> response, String houseType) {
        if (response == null || response.size() < 2) {
            log.info("LH API 응답이 비어있거나 구조가 올바르지 않음");
            return Collections.emptyList();
        }

        Map<String, Object> responseData = response.get(1);
        List<Map<String, Object>> dsList = (List<Map<String, Object>>) responseData.get("dsList");

        if (dsList == null || dsList.isEmpty()) {
            return Collections.emptyList();
        }

        return dsList.stream()
                .filter(entry -> "접수중".equals(entry.get("PAN_SS")))  // 접수중인 공고만
                .map(item -> buildLhSubscriptionInfo(item, houseType))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    /**
     * LH API 응답 파싱 (전체 데이터 - 날짜 필터링 없음)
     */
    @SuppressWarnings("unchecked")
    private List<LhSubscriptionInfo> parseAllLhResponse(List<Map<String, Object>> response, String houseType) {
        if (response == null || response.size() < 2) {
            log.info("LH API 응답이 비어있거나 구조가 올바르지 않음");
            return Collections.emptyList();
        }

        Map<String, Object> responseData = response.get(1);
        List<Map<String, Object>> dsList = (List<Map<String, Object>>) responseData.get("dsList");

        if (dsList == null || dsList.isEmpty()) {
            return Collections.emptyList();
        }

        return dsList.stream()
                .map(item -> buildLhSubscriptionInfo(item, houseType))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    /**
     * LH 데이터를 SubscriptionInfo로 변환
     */
    private Optional<LhSubscriptionInfo> buildLhSubscriptionInfo(Map<String, Object> item, String houseType) {
        String panNm = getString(item, "PAN_NM");

        return Optional.of(LhSubscriptionInfo.builder()
                .houseName(panNm + " [" + LH_PROVIDER_NAME + "]")
                .houseType(houseType)
                .area(getString(item, "CNP_CD_NM"))
                .announceDate(parseLhDate(getString(item, "PAN_NT_ST_DT")))
                .receiptEndDate(parseLhDate(getString(item, "CLSG_DT")))
                .detailUrl(getString(item, "DTL_URL"))
                .build());
    }


    /**
     * LH 날짜 파싱 ("2020.04.27" 또는 "20200427" 형식)
     */
    private LocalDate parseLhDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty() || "-".equals(dateStr)) {
            return null;
        }

        try {
            String cleanDateStr = dateStr.trim();

            if (cleanDateStr.contains(".")) {
                return LocalDate.parse(cleanDateStr, DateTimeFormatter.ofPattern("yyyy.MM.dd"));
            } else if (cleanDateStr.length() == 8) {
                return LocalDate.parse(cleanDateStr, LH_DATE_FORMATTER);
            } else if (cleanDateStr.contains("-")) {
                return LocalDate.parse(cleanDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }

            log.warn("지원하지 않는 LH 날짜 형식: {}", dateStr);
            return null;

        } catch (Exception e) {
            log.warn("LH 날짜 파싱 실패: {}", dateStr);
            return null;
        }
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
