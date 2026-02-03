package com.yunbok.houseping.adapter.out.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yunbok.houseping.infrastructure.util.DateParsingUtil;
import com.yunbok.houseping.domain.model.AreaCodeMapping;
import com.yunbok.houseping.domain.model.LhApiTypeCode;
import com.yunbok.houseping.domain.model.LhSubscriptionInfo;
import com.yunbok.houseping.domain.model.SubscriptionInfo;
import com.yunbok.houseping.domain.port.out.SubscriptionProvider;
import com.yunbok.houseping.infrastructure.config.SubscriptionProperties;
import com.yunbok.houseping.adapter.out.api.LhApiItem;
import com.yunbok.houseping.adapter.out.api.LhApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@ConditionalOnProperty(name = "feature.subscription.lh-api-enabled", havingValue = "true", matchIfMissing = false)
public class LhApiAdapter implements SubscriptionProvider {

    private static final String LH_PROVIDER_NAME = "LH";
    private static final String SOURCE_NAME = "LH";
    private static final String RECEIPT_STATUS_IN_PROGRESS = "접수중";

    /** 비주거용 공고 제외 키워드 */
    private static final Set<String> NON_RESIDENTIAL_KEYWORDS = Set.of(
            "상가", "어린이집", "가스충전소", "용지", "입점자", "임차운영자"
    );

    @Value("${lh.api.key}")
    private String apiKey;

    private final WebClient webClient;
    private final SubscriptionProperties properties;
    private final ObjectMapper objectMapper;

    public LhApiAdapter(@Qualifier("lhWebClient") WebClient webClient, SubscriptionProperties properties) {
        this.webClient = webClient;
        this.properties = properties;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getSourceName() {
        return SOURCE_NAME;
    }

    @Override
    public List<SubscriptionInfo> fetch(String areaName, LocalDate targetDate) {
        log.info("[LH API] Fetching area={}, date={}", areaName, targetDate);

        List<SubscriptionInfo> result = new ArrayList<>(fetchRegularApts(areaName));
        result.addAll(fetchNewlywedApts(areaName));
        result.addAll(fetchRentalApts(areaName));

        log.info("[LH API] area={} fetched {} items", areaName, result.size());
        return result;
    }

    @Override
    public List<SubscriptionInfo> fetchAll(String areaName) {
        try {
            log.info("[LH API] Fetching all for area={}", areaName);

            List<SubscriptionInfo> result = new ArrayList<>(fetchAllRegularApts(areaName));
            result.addAll(fetchAllNewlywedApts(areaName));
            result.addAll(fetchAllRentalApts(areaName));

            log.info("[LH API] area={} fetched {} items total", areaName, result.size());
            return result;
        } catch (Exception e) {
            log.error("[LH API] Fetch all failed: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private List<LhSubscriptionInfo> fetchRegularApts(String areaName) {
        return fetchLhSubscriptions(LhApiTypeCode.SALE_APT, areaName, true);
    }

    private List<LhSubscriptionInfo> fetchNewlywedApts(String areaName) {
        return fetchLhSubscriptions(LhApiTypeCode.NEWLYWED_APT, areaName, true);
    }

    private List<LhSubscriptionInfo> fetchRentalApts(String areaName) {
        return fetchLhSubscriptions(LhApiTypeCode.RENTAL_APT, areaName, true);
    }

    private List<LhSubscriptionInfo> fetchAllRegularApts(String areaName) {
        return fetchLhSubscriptions(LhApiTypeCode.SALE_APT, areaName, false);
    }

    private List<LhSubscriptionInfo> fetchAllNewlywedApts(String areaName) {
        return fetchLhSubscriptions(LhApiTypeCode.NEWLYWED_APT, areaName, false);
    }

    private List<LhSubscriptionInfo> fetchAllRentalApts(String areaName) {
        return fetchLhSubscriptions(LhApiTypeCode.RENTAL_APT, areaName, false);
    }

    private List<LhSubscriptionInfo> fetchLhSubscriptions(LhApiTypeCode apiTypeCode, String areaName, boolean filterInProgress) {
        String lhAreaCode = AreaCodeMapping.getLhAreaCodeByName(areaName);

        String responseStr = webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder
                            .queryParam("serviceKey", apiKey)
                            .queryParam("PG_SZ", properties.getApi().getPageSize())
                            .queryParam("PAGE", properties.getApi().getDefaultPage())
                            .queryParam("UPP_AIS_TP_CD", apiTypeCode.getTypeCode())
                            .queryParam("CNP_CD", lhAreaCode);
                    if (filterInProgress) {
                        builder.queryParam("PAN_SS", RECEIPT_STATUS_IN_PROGRESS);
                    }
                    return builder.build();
                })
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return parseLhResponse(responseStr, apiTypeCode.getDisplayName(), filterInProgress);
    }

    private List<LhSubscriptionInfo> parseLhResponse(String responseStr, String houseType, boolean filterInProgress) {
        if (responseStr == null || responseStr.isBlank()) {
            log.debug("[LH API] Empty response");
            return Collections.emptyList();
        }

        try {
            // LH API는 배열 형태로 응답하며, 두 번째 요소에 dsList가 포함됨
            List<Map<String, Object>> responseList = objectMapper.readValue(
                    responseStr, new TypeReference<List<Map<String, Object>>>() {});

            if (responseList == null || responseList.size() < 2) {
                log.debug("[LH API] Invalid response structure");
                return Collections.emptyList();
            }

            Map<String, Object> responseData = responseList.get(1);
            LhApiResponse lhResponse = objectMapper.convertValue(responseData, LhApiResponse.class);

            if (lhResponse == null || lhResponse.getItems().isEmpty()) {
                return Collections.emptyList();
            }

            return lhResponse.getItems().stream()
                    .filter(item -> !filterInProgress || RECEIPT_STATUS_IN_PROGRESS.equals(item.status()))
                    .filter(this::isResidential)
                    .map(item -> buildLhSubscriptionInfo(item, houseType))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();

        } catch (Exception e) {
            log.error("[LH API] Failed to parse response: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 주거용 공고인지 확인 (상가, 어린이집 등 비주거용 제외)
     */
    private boolean isResidential(LhApiItem item) {
        String projectName = item.projectName();
        if (projectName == null || projectName.isBlank()) {
            return false;
        }

        boolean isNonResidential = NON_RESIDENTIAL_KEYWORDS.stream()
                .anyMatch(projectName::contains);

        if (isNonResidential) {
            log.debug("[LH API] 비주거용 공고 제외: {}", projectName);
        }

        return !isNonResidential;
    }

    private Optional<LhSubscriptionInfo> buildLhSubscriptionInfo(LhApiItem item, String houseType) {
        String projectName = item.projectName();
        if (projectName == null || projectName.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(LhSubscriptionInfo.builder()
                .houseName(projectName)
                .houseType(houseType)
                .area(item.areaName())
                .announceDate(DateParsingUtil.parse(item.announceDate()))
                .receiptEndDate(DateParsingUtil.parse(item.closeDate()))
                .detailUrl(item.detailUrl())
                .build());
    }
}
