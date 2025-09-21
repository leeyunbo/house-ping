package com.yunbok.houseping.infrastructure.adapter.outbound.api;

import com.yunbok.houseping.domain.model.LhSubscriptionInfo;
import com.yunbok.houseping.domain.model.SubscriptionInfo;
import com.yunbok.houseping.domain.port.outbound.SubscriptionDataProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.http.HttpHeaders;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * LH(한국토지주택공사) API 어댑터
 */
@Slf4j
@Component
public class LhApiAdapter implements SubscriptionDataProvider {

    private static final DateTimeFormatter LH_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String LH_PROVIDER_NAME = "LH";

    @Value("${lh.api.key}")
    private String apiKey;

    private final WebClient webClient;

    public LhApiAdapter(@Qualifier("lhWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public List<SubscriptionInfo> fetch(String areaCode, LocalDate targetDate) {
        List<SubscriptionInfo> allSubscriptions = new ArrayList<>(fetchRegularApts(areaCode, targetDate));
        allSubscriptions.addAll(fetchNewlywedApts(areaCode, targetDate));
        allSubscriptions.addAll(fetchRemainingApts(areaCode, targetDate));
        return allSubscriptions;
    }

    private List<LhSubscriptionInfo> fetchRegularApts(String areaCode, LocalDate targetDate) {
        // LH의 분양주택 (공고유형코드: 05)
        return fetchLhSubscriptions("05", areaCode, targetDate, "LH 분양주택");
    }

    private List<LhSubscriptionInfo> fetchNewlywedApts(String areaCode, LocalDate targetDate) {
        // LH의 신혼희망타운 (공고유형코드: 39)
        return fetchLhSubscriptions("39", areaCode, targetDate, "LH 신혼희망타운");
    }

    private List<LhSubscriptionInfo> fetchRemainingApts(String areaCode, LocalDate targetDate) {
        // LH의 임대주택 (공고유형코드: 06)
        return fetchLhSubscriptions("06", areaCode, targetDate, "LH 임대주택");
    }

    /**
     * LH API 공통 호출 메서드
     */
    private List<LhSubscriptionInfo> fetchLhSubscriptions(String typeCode, String areaCode, LocalDate targetDate, String houseType) {
        String dateStr = targetDate.format(LH_DATE_FORMATTER);
        String lhAreaCode = convertToLhAreaCode(areaCode); // 청약Home 지역코드를 LH 지역코드로 변환

        log.info("LH API 호출 시작 - typeCode: {}, areaCode: {}, date: {}", typeCode, lhAreaCode, dateStr);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("serviceKey", apiKey)
                        .queryParam("PG_SZ", 100)
                        .queryParam("PAGE", 1)
                        .queryParam("UPP_AIS_TP_CD", typeCode)
                        .queryParam("CNP_CD", lhAreaCode)
                        .queryParam("PAN_ST_DT", dateStr)
                        .build())
                .retrieve()
                .bodyToMono(List.class)
                .block();

        log.info("LH API 응답 (typeCode: {}, areaCode: {}, date: {}): 응답 크기 = {}",
                typeCode, lhAreaCode, dateStr, response != null ? response.size() : 0);

        return parseLhResponse(response, houseType);
    }

    /**
     * LH API 응답 파싱
     */
    @SuppressWarnings("unchecked")
    private List<LhSubscriptionInfo> parseLhResponse(List<Map<String, Object>> response, String houseType) {
        if (response == null || response.size() < 2) {
            log.info("LH API 응답이 비어있거나 구조가 올바르지 않음");
            return Collections.emptyList();
        }

        Map<String, Object> responseData = (Map<String, Object>) response.get(1);
        List<Map<String, Object>> dsList = (List<Map<String, Object>>) responseData.get("dsList");

        if (dsList == null || dsList.isEmpty()) {
            return Collections.emptyList();
        }

        List<LhSubscriptionInfo> subscriptions = dsList.stream()
                .filter(entry -> !entry.get("PAN_SS").equals("접수마감"))
                .map(item -> buildLhSubscriptionInfo(item, houseType))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        log.info("LH API 파싱 완료: {}개 청약 정보 생성", subscriptions.size());
        return subscriptions;
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
     * 청약Home 지역코드를 LH 지역코드로 변환
     */
    private String convertToLhAreaCode(String applyHomeAreaCode) {
        // 청약Home 지역코드 -> LH 지역코드 매핑
        return switch (applyHomeAreaCode) {
            case "100" -> "11";    // 서울 -> 서울특별시
            case "410" -> "41";    // 경기 -> 경기도
            case "400" -> "28";    // 인천 -> 인천광역시
            case "600" -> "26";    // 부산 -> 부산광역시
            case "700" -> "27";    // 대구 -> 대구광역시
            case "300" -> "30";    // 대전 -> 대전광역시
            case "500" -> "29";    // 광주 -> 광주광역시
            case "680" -> "31";    // 울산 -> 울산광역시
            case "338" -> "36110"; // 세종 -> 세종특별자치시
            case "200" -> "42";    // 강원 -> 강원도
            case "360" -> "43";    // 충북 -> 충청북도
            case "312" -> "44";    // 충남 -> 충청남도
            case "560" -> "52";    // 전북 -> 전북특별자치도
            case "513" -> "46";    // 전남 -> 전라남도
            case "712" -> "47";    // 경북 -> 경상북도
            case "621" -> "48";    // 경남 -> 경상남도
            case "690" -> "50";    // 제주 -> 제주특별자치도
            default -> applyHomeAreaCode; // 매핑되지 않은 경우 원본 사용
        };
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
                // "2020.04.27" 형식
                return LocalDate.parse(cleanDateStr, DateTimeFormatter.ofPattern("yyyy.MM.dd"));
            } else if (cleanDateStr.length() == 8) {
                // "20200427" 형식
                return LocalDate.parse(cleanDateStr, LH_DATE_FORMATTER);
            } else if (cleanDateStr.contains("-")) {
                // "2020-04-27" 형식
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
