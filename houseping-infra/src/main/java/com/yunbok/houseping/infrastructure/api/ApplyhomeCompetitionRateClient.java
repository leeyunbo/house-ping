package com.yunbok.houseping.infrastructure.api;

import com.yunbok.houseping.support.external.CompetitionRateItem;
import com.yunbok.houseping.support.external.CompetitionRateResponse;
import com.yunbok.houseping.core.domain.CompetitionRate;
import com.yunbok.houseping.core.port.CompetitionRateProvider;
import com.yunbok.houseping.config.SubscriptionProperties;
import com.yunbok.houseping.support.util.ApiRateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 청약홈 경쟁률 API 어댑터
 * Base URL: <a href="https://api.odcloud.kr/api/ApplyhomeInfoCmpetRtSvc/v1/">...</a>
 */
@Slf4j
@Component
@ConditionalOnProperty(
        name = "feature.subscription.applyhome-competition-enabled",
        havingValue = "true"
)
public class ApplyhomeCompetitionRateClient implements CompetitionRateProvider {

    private static final int API_RATE_LIMIT_MS = 100;

    private final WebClient webClient;
    private final SubscriptionProperties properties;
    private final String apiKey;

    public ApplyhomeCompetitionRateClient(
            @Qualifier("competitionRateWebClient") WebClient webClient,
            SubscriptionProperties properties,
            @Value("${applyhome.api.key}") String apiKey) {
        this.webClient = webClient;
        this.properties = properties;
        this.apiKey = apiKey;
    }

    public List<CompetitionRate> fetchAll() {
        log.info("[경쟁률 API] 전체 조회 시작");

        // APT 경쟁률만 조회 (잔여세대는 경쟁률이 미달 표시로 와서 제외)
        List<CompetitionRate> allRates = new ArrayList<>(fetchAptCompetitionRates());

        log.info("[경쟁률 API] 전체 조회 완료 - {}건", allRates.size());
        return allRates;
    }

    /**
     * APT 분양정보/경쟁률 전체 조회 (모든 페이지)
     */
    private List<CompetitionRate> fetchAptCompetitionRates() {
        List<CompetitionRate> allRates = new ArrayList<>();
        int pageSize = properties.getApi().getPageSize();

        try {
            // 첫 페이지 조회하여 totalCount 확인
            CompetitionRateResponse firstResponse = fetchPage("/getAPTLttotPblancCmpet", 1, pageSize);
            int totalCount = firstResponse.totalCount() != null ? firstResponse.totalCount() : 0;
            allRates.addAll(parseResponse(firstResponse));

            int totalPages = (totalCount + pageSize - 1) / pageSize;
            log.info("[경쟁률 API] APT 경쟁률 페이지 1/{} 조회 - {}건 (총 {}건)",
                    totalPages, firstResponse.currentCount(), totalCount);

            // 나머지 페이지 조회
            for (int page = 2; page <= totalPages; page++) {
                try {
                    CompetitionRateResponse response = fetchPage("/getAPTLttotPblancCmpet", page, pageSize);
                    allRates.addAll(parseResponse(response));
                    log.info("[경쟁률 API] APT 경쟁률 페이지 {}/{} 조회 - {}건",
                            page, totalPages, response.currentCount());
                } catch (WebClientResponseException e) {
                    log.warn("[경쟁률 API] 페이지 {} 조회 실패 - status: {}", page, e.getStatusCode());
                }
                ApiRateLimiter.delay(API_RATE_LIMIT_MS);
            }

            log.info("[경쟁률 API] APT 경쟁률 전체 조회 완료 - 총 {}건", allRates.size());
            return allRates;

        } catch (Exception e) {
            log.warn("[경쟁률 API] APT 경쟁률 조회 중 오류 - {}", e.getMessage());
            return allRates;
        }
    }

    /**
     * 단일 페이지 조회
     */
    private CompetitionRateResponse fetchPage(String path, int page, int pageSize) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam("page", page)
                        .queryParam("perPage", pageSize)
                        .queryParam("serviceKey", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(CompetitionRateResponse.class)
                .block();
    }

    private List<CompetitionRate> parseResponse(CompetitionRateResponse response) {
        if (response == null || response.data() == null) {
            return Collections.emptyList();
        }

        return response.data().stream()
                .map(this::toCompetitionRate)
                .toList();
    }

    private CompetitionRate toCompetitionRate(CompetitionRateItem item) {
        return CompetitionRate.builder()
                .houseManageNo(item.houseManageNo())
                .pblancNo(item.pblancNo())
                .houseType(item.houseType())
                .supplyCount(item.supplyCount())
                .requestCount(parseInteger(item.requestCount()))
                .competitionRate(parseBigDecimal(item.competitionRate()))
                .residenceArea(item.residenceArea())
                .rank(item.rank())
                .build();
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank() || "-".equals(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value.replace(",", "").trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isBlank() || "-".equals(value)) {
            return null;
        }
        try {
            return new BigDecimal(value.replace(",", "").trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
