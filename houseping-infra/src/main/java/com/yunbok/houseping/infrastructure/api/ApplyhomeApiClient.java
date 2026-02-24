package com.yunbok.houseping.infrastructure.api;

import com.yunbok.houseping.support.util.DateParsingUtil;
import com.yunbok.houseping.infrastructure.dto.ApplyHomeSubscriptionInfo;
import com.yunbok.houseping.core.domain.HouseType;
import com.yunbok.houseping.core.domain.Subscription;
import com.yunbok.houseping.core.domain.SubscriptionSource;
import com.yunbok.houseping.core.port.SubscriptionProvider;
import com.yunbok.houseping.config.SubscriptionProperties;
import com.yunbok.houseping.support.external.ApplyhomeAptResponse;
import com.yunbok.houseping.support.external.ApplyhomeRemainingResponse;
import com.yunbok.houseping.support.external.ApplyhomeArbitraryResponse;
import com.yunbok.houseping.support.external.ApplyhomeSubscriptionItem;
import com.yunbok.houseping.support.external.ApplyhomePriceDetailItem;
import com.yunbok.houseping.support.external.ApplyhomePriceDetailResponse;
import com.yunbok.houseping.entity.SubscriptionPriceEntity;
import com.yunbok.houseping.repository.SubscriptionPriceRepository;
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
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

@Slf4j
@Component
@ConditionalOnProperty(
        name = "feature.subscription.applyhome-api-enabled",
        havingValue = "true"
)
public class ApplyhomeApiClient implements SubscriptionProvider {

    private static final String SOURCE_NAME = SubscriptionSource.APPLYHOME.getValue();
    private static final int PRICE_DETAIL_PAGE_SIZE = 100;

    private static final List<HouseType> FETCH_TYPES = List.of(
            HouseType.APT, HouseType.PRIVATE_PRE_SUBSCRIPTION, HouseType.NEWLYWED_TOWN,
            HouseType.REMAINING, HouseType.ARBITRARY
    );

    private static final List<String> FETCH_TYPE_LABELS = List.of(
            "일반APT", "민간사전", "신혼희망", "잔여세대", "임의공급"
    );

    @Value("${applyhome.api.key}")
    private String apiKey;

    private final WebClient webClient;
    private final SubscriptionProperties properties;
    private final SubscriptionPriceRepository priceRepository;

    public ApplyhomeApiClient(
            @Qualifier(value = "applyHomeWebClient") WebClient webClient,
            SubscriptionProperties properties,
            SubscriptionPriceRepository priceRepository) {
        this.webClient = webClient;
        this.properties = properties;
        this.priceRepository = priceRepository;
    }

    public String getSourceName() {
        return SOURCE_NAME;
    }

    public List<Subscription> fetch(String areaName, LocalDate targetDate) {
        log.info("[청약Home API] {} 지역 데이터 수집 시작 (날짜: {})", areaName, targetDate);

        List<ApplyHomeSubscriptionInfo> allDtos = new ArrayList<>();
        for (int i = 0; i < FETCH_TYPES.size(); i++) {
            HouseType type = FETCH_TYPES.get(i);
            String label = FETCH_TYPE_LABELS.get(i);
            allDtos.addAll(fetchSafely(() -> fetchSubscriptions(type, areaName, targetDate), label));
        }

        log.info("[청약Home API] {} 지역에서 {}개 데이터 수집 완료", areaName, allDtos.size());
        return allDtos.stream().map(ApplyHomeSubscriptionInfo::toSubscription).toList();
    }

    public List<Subscription> fetchAll(String areaName) {
        log.info("[청약Home API] {} 지역 전체 데이터 수집 시작 (DB 동기화용)", areaName);

        List<ApplyHomeSubscriptionInfo> allDtos = new ArrayList<>();
        for (int i = 0; i < FETCH_TYPES.size(); i++) {
            HouseType type = FETCH_TYPES.get(i);
            String label = FETCH_TYPE_LABELS.get(i);
            allDtos.addAll(fetchSafely(() -> fetchSubscriptions(type, areaName, null), label));
        }

        log.info("[청약Home API] {} 지역에서 총 {}개 데이터 수집 완료", areaName, allDtos.size());
        return allDtos.stream().map(ApplyHomeSubscriptionInfo::toSubscription).toList();
    }

    /**
     * 개별 API 호출을 안전하게 실행 (실패해도 다른 API 수집 계속)
     */
    private List<ApplyHomeSubscriptionInfo> fetchSafely(
            Supplier<List<ApplyHomeSubscriptionInfo>> fetcher, String type) {
        try {
            return fetcher.get();
        } catch (Exception e) {
            log.warn("[청약Home API] {} 수집 실패 (계속 진행): {}", type, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 통합 청약 데이터 조회 — targetDate가 null이면 전체 조회
     */
    private List<ApplyHomeSubscriptionInfo> fetchSubscriptions(HouseType houseType, String areaName, LocalDate targetDate) {
        List<? extends ApplyhomeSubscriptionItem> items = fetchItems(houseType, areaName);
        return parseResponse(items, houseType.getDisplayName(), targetDate);
    }

    /**
     * HouseType별 API 호출 및 아이템 목록 반환
     */
    private List<? extends ApplyhomeSubscriptionItem> fetchItems(HouseType houseType, String areaName) {
        if (houseType.usesHouseSecd()) {
            ApplyhomeAptResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(houseType.getDetailPath())
                            .queryParam("page", properties.getApi().getDefaultPage())
                            .queryParam("perPage", properties.getApi().getPageSize())
                            .queryParam("cond[HOUSE_SECD::EQ]", houseType.getHouseSecd())
                            .queryParam("cond[SUBSCRPT_AREA_CODE_NM::EQ]", areaName)
                            .queryParam("serviceKey", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(ApplyhomeAptResponse.class)
                    .block();
            return response != null ? response.getData() : Collections.emptyList();
        }

        if (houseType == HouseType.REMAINING) {
            ApplyhomeRemainingResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(houseType.getDetailPath())
                            .queryParam("page", properties.getApi().getDefaultPage())
                            .queryParam("perPage", properties.getApi().getPageSize())
                            .queryParam("cond[SUBSCRPT_AREA_CODE_NM::EQ]", areaName)
                            .queryParam("serviceKey", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(ApplyhomeRemainingResponse.class)
                    .block();
            return response != null ? response.getData() : Collections.emptyList();
        }

        // ARBITRARY
        ApplyhomeArbitraryResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(houseType.getDetailPath())
                        .queryParam("page", properties.getApi().getDefaultPage())
                        .queryParam("perPage", properties.getApi().getPageSize())
                        .queryParam("cond[SUBSCRPT_AREA_CODE_NM::EQ]", areaName)
                        .queryParam("serviceKey", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(ApplyhomeArbitraryResponse.class)
                .block();
        return response != null ? response.getData() : Collections.emptyList();
    }

    /**
     * 통합 응답 파싱 — targetDate가 null이면 날짜 필터 없음
     */
    private List<ApplyHomeSubscriptionInfo> parseResponse(
            List<? extends ApplyhomeSubscriptionItem> items, String houseType, LocalDate targetDate) {
        if (items == null || items.isEmpty()) return Collections.emptyList();

        return items.stream()
                .filter(item -> targetDate == null || isReceiptDateInRange(item.receiptStartDate(), targetDate))
                .map(item -> buildSubscriptionInfo(item, houseType))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private boolean isReceiptDateInRange(String dateStr, LocalDate targetDate) {
        if (DateParsingUtil.isBlankOrDash(dateStr)) {
            return false;
        }
        LocalDate date = DateParsingUtil.parse(dateStr);
        return date != null && date.equals(targetDate);
    }

    /**
     * 통합 SubscriptionInfo 생성
     */
    private Optional<ApplyHomeSubscriptionInfo> buildSubscriptionInfo(ApplyhomeSubscriptionItem item, String houseType) {
        try {
            return Optional.of(ApplyHomeSubscriptionInfo.builder()
                    .houseManageNo(Objects.toString(item.houseManageNo(), ""))
                    .pblancNo(Objects.toString(item.pblancNo(), ""))
                    .houseName(Objects.toString(item.houseName(), ""))
                    .houseType(houseType)
                    .area(Objects.toString(item.areaName(), ""))
                    .announceDate(DateParsingUtil.parse(item.announceDate()))
                    .receiptStartDate(DateParsingUtil.parse(item.receiptStartDate()))
                    .receiptEndDate(DateParsingUtil.parse(item.receiptEndDate()))
                    .winnerAnnounceDate(DateParsingUtil.parse(item.winnerAnnounceDate()))
                    .homepageUrl(item.homepageUrl())
                    .detailUrl(item.detailUrl())
                    .contact(item.contact())
                    .totalSupplyCount(item.totalSupplyCount() != null ? item.totalSupplyCount() : 0)
                    .address(item.address())
                    .zipCode(item.zipCode())
                    .build());
        } catch (Exception e) {
            log.warn("SubscriptionInfo 생성 실패: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 분양가 상세 정보 조회 및 저장
     */
    public void fetchAndSavePriceDetails(String houseManageNo, String pblancNo, String houseType) {
        try {
            HouseType type = HouseType.fromDisplayName(houseType);
            List<ApplyhomePriceDetailItem> priceDetails = fetchPriceDetails(
                    type.getPricePath(), houseManageNo, pblancNo);

            for (ApplyhomePriceDetailItem item : priceDetails) {
                if (priceRepository.findByHouseManageNoAndPblancNoAndModelNo(
                        item.houseManageNo(), item.pblancNo(), item.modelNo()).isEmpty()) {

                    SubscriptionPriceEntity entity = SubscriptionPriceEntity.builder()
                            .houseManageNo(item.houseManageNo())
                            .pblancNo(item.pblancNo())
                            .modelNo(item.modelNo())
                            .houseType(item.houseType())
                            .supplyArea(item.supplyArea())
                            .supplyCount(item.supplyCount())
                            .specialSupplyCount(item.specialSupplyCount())
                            .topAmount(item.topAmount())
                            .build();
                    entity.calculatePricePerPyeong();
                    priceRepository.save(entity);
                }
            }

            log.info("[분양가] {}({}) 분양가 {}건 저장 완료", houseManageNo, houseType, priceDetails.size());
        } catch (Exception e) {
            log.warn("[분양가] {}({}) 분양가 조회 실패: {}", houseManageNo, houseType, e.getMessage());
        }
    }

    /**
     * 분양가 상세 조회
     */
    private List<ApplyhomePriceDetailItem> fetchPriceDetails(String path, String houseManageNo, String pblancNo) {
        ApplyhomePriceDetailResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam("page", 1)
                        .queryParam("perPage", PRICE_DETAIL_PAGE_SIZE)
                        .queryParam("cond[HOUSE_MANAGE_NO::EQ]", houseManageNo)
                        .queryParam("cond[PBLANC_NO::EQ]", pblancNo)
                        .queryParam("serviceKey", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(ApplyhomePriceDetailResponse.class)
                .block();

        return response != null ? response.getData() : Collections.emptyList();
    }
}
