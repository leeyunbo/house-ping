package com.yunbok.houseping.externalapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yunbok.houseping.core.port.ApartmentGeocodingPort;
import com.yunbok.houseping.core.port.ApartmentLocationPort;
import com.yunbok.houseping.support.util.ApiRateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

/**
 * 카카오 로컬 API를 통한 아파트 좌표 조회 + 저장
 */
@Slf4j
@Component
public class KakaoGeocodingClient implements ApartmentGeocodingPort {

    private static final String KAKAO_KEYWORD_SEARCH_URL = "https://dapi.kakao.com/v2/local/search/keyword.json";

    @Value("${kakao.api.rest-key:}")
    private String restApiKey;

    private final WebClient webClient;
    private final ApartmentLocationPort locationPort;

    public KakaoGeocodingClient(WebClient.Builder webClientBuilder, ApartmentLocationPort locationPort) {
        this.webClient = webClientBuilder.build();
        this.locationPort = locationPort;
    }

    @Override
    public void geocodeIfAbsent(String aptName, String lawdCd, String dongName, String jibun) {
        if (restApiKey == null || restApiKey.isBlank()) {
            return;
        }
        if (locationPort.exists(aptName, lawdCd)) {
            return;
        }

        try {
            String query = dongName + " " + aptName;
            KakaoSearchResponse response = webClient.get()
                    .uri(KAKAO_KEYWORD_SEARCH_URL, uriBuilder -> uriBuilder
                            .queryParam("query", query)
                            .queryParam("size", 1)
                            .build())
                    .header("Authorization", "KakaoAK " + restApiKey)
                    .retrieve()
                    .bodyToMono(KakaoSearchResponse.class)
                    .block();

            if (response != null && response.documents != null && !response.documents.isEmpty()) {
                KakaoDocument doc = response.documents.getFirst();
                double lat = Double.parseDouble(doc.y);
                double lng = Double.parseDouble(doc.x);
                locationPort.save(aptName, lawdCd, dongName, jibun, lat, lng);
                log.debug("[api.kakao] 좌표 저장: {} → ({}, {})", aptName, lat, lng);
            } else {
                log.debug("[api.kakao] 좌표 못 찾음: {}", query);
            }
        } catch (Exception e) {
            log.warn("[api.kakao] 지오코딩 실패: {} - {}", aptName, e.getMessage());
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record KakaoSearchResponse(@JsonProperty("documents") List<KakaoDocument> documents) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record KakaoDocument(@JsonProperty("x") String x, @JsonProperty("y") String y) {}
}
