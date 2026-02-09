package com.yunbok.houseping.adapter.api;

import com.yunbok.houseping.support.util.DateParsingUtil;
import com.yunbok.houseping.adapter.dto.ApplyHomeSubscriptionInfo;
import com.yunbok.houseping.core.domain.HouseType;
import com.yunbok.houseping.core.domain.SubscriptionSource;
import com.yunbok.houseping.adapter.dto.SubscriptionInfo;
import com.yunbok.houseping.core.port.SubscriptionProvider;
import com.yunbok.houseping.config.SubscriptionProperties;
import com.yunbok.houseping.support.external.ApplyhomeApiItem;
import com.yunbok.houseping.support.external.ApplyhomeAptResponse;
import com.yunbok.houseping.support.external.ApplyhomeRemainingItem;
import com.yunbok.houseping.support.external.ApplyhomeRemainingResponse;
import com.yunbok.houseping.support.external.ApplyhomeArbitraryItem;
import com.yunbok.houseping.support.external.ApplyhomeArbitraryResponse;
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
import java.util.Optional;

@Slf4j
@Component
@ConditionalOnProperty(
        name = "feature.subscription.applyhome-api-enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class ApplyhomeApiAdapter implements SubscriptionProvider {

    private static final String SOURCE_NAME = SubscriptionSource.APPLYHOME.getValue();

    @Value("${applyhome.api.key}")
    private String apiKey;

    private final WebClient webClient;
    private final SubscriptionProperties properties;
    private final SubscriptionPriceRepository priceRepository;

    public ApplyhomeApiAdapter(
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

    public List<SubscriptionInfo> fetch(String areaName, LocalDate targetDate) {
        log.info("[청약Home API] {} 지역 데이터 수집 시작 (날짜: {})", areaName, targetDate);

        List<SubscriptionInfo> allSubscriptions = new ArrayList<>(fetchRegularApts(areaName, targetDate));
        allSubscriptions.addAll(fetchPrivatePreApts(areaName, targetDate));
        allSubscriptions.addAll(fetchNewlywedApts(areaName, targetDate));
        allSubscriptions.addAll(fetchRemainingApts(areaName, targetDate));
        allSubscriptions.addAll(fetchArbitraryApts(areaName, targetDate));

        log.info("[청약Home API] {} 지역에서 {}개 데이터 수집 완료", areaName, allSubscriptions.size());
        return allSubscriptions;
    }

    public List<SubscriptionInfo> fetchAll(String areaName) {
        try {
            log.info("[청약Home API] {} 지역 전체 데이터 수집 시작 (DB 동기화용)", areaName);

            List<SubscriptionInfo> allSubscriptions = new ArrayList<>(fetchAllRegularApts(areaName));
            allSubscriptions.addAll(fetchAllPrivatePreApts(areaName));
            allSubscriptions.addAll(fetchAllNewlywedApts(areaName));
            allSubscriptions.addAll(fetchAllRemainingApts(areaName));
            allSubscriptions.addAll(fetchAllArbitraryApts(areaName));

            log.info("[청약Home API] {} 지역에서 총 {}개 데이터 수집 완료", areaName, allSubscriptions.size());
            return allSubscriptions;

        } catch (Exception e) {
            log.error("[청약Home API] 전체 데이터 수집 실패: {}", e.getMessage(), e);
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

    private List<ApplyHomeSubscriptionInfo> fetchAllRegularApts(String areaName) {
        return fetchAllAptSubscriptions(HouseType.APT, areaName);
    }

    private List<ApplyHomeSubscriptionInfo> fetchAllPrivatePreApts(String areaName) {
        return fetchAllAptSubscriptions(HouseType.PRIVATE_PRE_SUBSCRIPTION, areaName);
    }

    private List<ApplyHomeSubscriptionInfo> fetchAllNewlywedApts(String areaName) {
        return fetchAllAptSubscriptions(HouseType.NEWLYWED_TOWN, areaName);
    }

    private List<ApplyHomeSubscriptionInfo> fetchAllRemainingApts(String areaName) {
        return fetchAllRemainingAptSubscriptions(areaName);
    }

    private List<ApplyHomeSubscriptionInfo> fetchArbitraryApts(String areaName, LocalDate targetDate) {
        return fetchArbitraryAptSubscriptions(areaName, targetDate);
    }

    private List<ApplyHomeSubscriptionInfo> fetchAllArbitraryApts(String areaName) {
        return fetchAllArbitraryAptSubscriptions(areaName);
    }

    /**
     * 일반 APT API 호출 (특정 날짜)
     */
    private List<ApplyHomeSubscriptionInfo> fetchAptSubscriptions(HouseType houseType, String areaName, LocalDate targetDate) {
        ApplyhomeAptResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/getAPTLttotPblancDetail")
                        .queryParam("page", properties.getApi().getDefaultPage())
                        .queryParam("perPage", properties.getApi().getPageSize())
                        .queryParam("cond[HOUSE_SECD::EQ]", houseType.getHouseSecd())
                        .queryParam("cond[SUBSCRPT_AREA_CODE_NM::EQ]", areaName)
                        .queryParam("serviceKey", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(ApplyhomeAptResponse.class)
                .block();

        return parseAptResponse(response, houseType.getDisplayName(), targetDate);
    }

    /**
     * 잔여세대 APT API 호출 (특정 날짜)
     */
    private List<ApplyHomeSubscriptionInfo> fetchRemainingAptSubscriptions(String areaName, LocalDate targetDate) {
        ApplyhomeRemainingResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/getRemndrLttotPblancDetail")
                        .queryParam("page", properties.getApi().getDefaultPage())
                        .queryParam("perPage", properties.getApi().getPageSize())
                        .queryParam("cond[SUBSCRPT_AREA_CODE_NM::EQ]", areaName)
                        .queryParam("serviceKey", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(ApplyhomeRemainingResponse.class)
                .block();

        return parseRemainingResponse(response, targetDate);
    }

    /**
     * 일반 APT API 호출 (전체 데이터)
     */
    private List<ApplyHomeSubscriptionInfo> fetchAllAptSubscriptions(HouseType houseType, String areaName) {
        ApplyhomeAptResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/getAPTLttotPblancDetail")
                        .queryParam("page", properties.getApi().getDefaultPage())
                        .queryParam("perPage", properties.getApi().getPageSize())
                        .queryParam("cond[HOUSE_SECD::EQ]", houseType.getHouseSecd())
                        .queryParam("cond[SUBSCRPT_AREA_CODE_NM::EQ]", areaName)
                        .queryParam("serviceKey", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(ApplyhomeAptResponse.class)
                .block();

        return parseAllAptResponse(response, houseType.getDisplayName());
    }

    /**
     * 잔여세대 APT API 호출 (전체 데이터)
     */
    private List<ApplyHomeSubscriptionInfo> fetchAllRemainingAptSubscriptions(String areaName) {
        ApplyhomeRemainingResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/getRemndrLttotPblancDetail")
                        .queryParam("page", properties.getApi().getDefaultPage())
                        .queryParam("perPage", properties.getApi().getPageSize())
                        .queryParam("cond[SUBSCRPT_AREA_CODE_NM::EQ]", areaName)
                        .queryParam("serviceKey", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(ApplyhomeRemainingResponse.class)
                .block();

        return parseAllRemainingResponse(response);
    }

    /**
     * 임의공급 APT API 호출 (특정 날짜)
     */
    private List<ApplyHomeSubscriptionInfo> fetchArbitraryAptSubscriptions(String areaName, LocalDate targetDate) {
        ApplyhomeArbitraryResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/getArbLttotPblancDetail")
                        .queryParam("page", properties.getApi().getDefaultPage())
                        .queryParam("perPage", properties.getApi().getPageSize())
                        .queryParam("cond[SUBSCRPT_AREA_CODE_NM::EQ]", areaName)
                        .queryParam("serviceKey", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(ApplyhomeArbitraryResponse.class)
                .block();

        return parseArbitraryResponse(response, targetDate);
    }

    /**
     * 임의공급 APT API 호출 (전체 데이터)
     */
    private List<ApplyHomeSubscriptionInfo> fetchAllArbitraryAptSubscriptions(String areaName) {
        ApplyhomeArbitraryResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/getArbLttotPblancDetail")
                        .queryParam("page", properties.getApi().getDefaultPage())
                        .queryParam("perPage", properties.getApi().getPageSize())
                        .queryParam("cond[SUBSCRPT_AREA_CODE_NM::EQ]", areaName)
                        .queryParam("serviceKey", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(ApplyhomeArbitraryResponse.class)
                .block();

        return parseAllArbitraryResponse(response);
    }

    /**
     * 일반 APT API 응답 파싱
     */
    private List<ApplyHomeSubscriptionInfo> parseAptResponse(ApplyhomeAptResponse response, String houseType, LocalDate targetDate) {
        if (response == null) return Collections.emptyList();

        return response.getData().stream()
                .filter(item -> isReceiptDateInRange(item.receiptStartDate(), targetDate))
                .map(item -> buildSubscriptionInfo(item, houseType))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    /**
     * 잔여세대 API 응답 파싱
     */
    private List<ApplyHomeSubscriptionInfo> parseRemainingResponse(ApplyhomeRemainingResponse response, LocalDate targetDate) {
        if (response == null) return Collections.emptyList();

        return response.getData().stream()
                .filter(item -> isReceiptDateInRange(item.receiptStartDate(), targetDate))
                .map(this::buildRemainingSubscriptionInfo)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    /**
     * 일반 APT API 응답 파싱 (전체 데이터 - 날짜 필터링 없음)
     */
    private List<ApplyHomeSubscriptionInfo> parseAllAptResponse(ApplyhomeAptResponse response, String houseType) {
        if (response == null) return Collections.emptyList();

        return response.getData().stream()
                .map(item -> buildSubscriptionInfo(item, houseType))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    /**
     * 잔여세대 API 응답 파싱 (전체 데이터 - 날짜 필터링 없음)
     */
    private List<ApplyHomeSubscriptionInfo> parseAllRemainingResponse(ApplyhomeRemainingResponse response) {
        if (response == null) return Collections.emptyList();

        return response.getData().stream()
                .map(this::buildRemainingSubscriptionInfo)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    /**
     * 임의공급 API 응답 파싱
     */
    private List<ApplyHomeSubscriptionInfo> parseArbitraryResponse(ApplyhomeArbitraryResponse response, LocalDate targetDate) {
        if (response == null) return Collections.emptyList();

        return response.getData().stream()
                .filter(item -> isReceiptDateInRange(item.receiptStartDate(), targetDate))
                .map(this::buildArbitrarySubscriptionInfo)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    /**
     * 임의공급 API 응답 파싱 (전체 데이터 - 날짜 필터링 없음)
     */
    private List<ApplyHomeSubscriptionInfo> parseAllArbitraryResponse(ApplyhomeArbitraryResponse response) {
        if (response == null) return Collections.emptyList();

        return response.getData().stream()
                .map(this::buildArbitrarySubscriptionInfo)
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
     * 일반 APT SubscriptionInfo 생성
     */
    private Optional<ApplyHomeSubscriptionInfo> buildSubscriptionInfo(ApplyhomeApiItem item, String houseType) {
        try {
            return Optional.of(ApplyHomeSubscriptionInfo.builder()
                    .houseManageNo(nullToEmpty(item.houseManageNo()))
                    .pblancNo(nullToEmpty(item.pblancNo()))
                    .houseName(nullToEmpty(item.houseName()))
                    .houseType(houseType)
                    .area(nullToEmpty(item.areaName()))
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
     * 잔여세대 SubscriptionInfo 생성
     */
    private Optional<ApplyHomeSubscriptionInfo> buildRemainingSubscriptionInfo(ApplyhomeRemainingItem item) {
        try {
            return Optional.of(ApplyHomeSubscriptionInfo.builder()
                    .houseManageNo(nullToEmpty(item.houseManageNo()))
                    .pblancNo(nullToEmpty(item.pblancNo()))
                    .houseName(nullToEmpty(item.houseName()))
                    .houseType(HouseType.REMAINING.getDisplayName())
                    .area(nullToEmpty(item.areaName()))
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
            log.warn("잔여세대 SubscriptionInfo 생성 실패: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 임의공급 SubscriptionInfo 생성
     */
    private Optional<ApplyHomeSubscriptionInfo> buildArbitrarySubscriptionInfo(ApplyhomeArbitraryItem item) {
        try {
            return Optional.of(ApplyHomeSubscriptionInfo.builder()
                    .houseManageNo(nullToEmpty(item.houseManageNo()))
                    .pblancNo(nullToEmpty(item.pblancNo()))
                    .houseName(nullToEmpty(item.houseName()))
                    .houseType(HouseType.ARBITRARY.getDisplayName())
                    .area(nullToEmpty(item.areaName()))
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
            log.warn("임의공급 SubscriptionInfo 생성 실패: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 분양가 상세 정보 조회 및 저장
     */
    public void fetchAndSavePriceDetails(String houseManageNo, String pblancNo, String houseType) {
        try {
            List<ApplyhomePriceDetailItem> priceDetails;

            if (HouseType.REMAINING.getDisplayName().equals(houseType)) {
                priceDetails = fetchRemainingPriceDetails(houseManageNo, pblancNo);
            } else if (HouseType.ARBITRARY.getDisplayName().equals(houseType)) {
                priceDetails = fetchArbitraryPriceDetails(houseManageNo, pblancNo);
            } else {
                priceDetails = fetchAptPriceDetails(houseManageNo, pblancNo);
            }

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
     * APT 분양가 상세 조회
     */
    private List<ApplyhomePriceDetailItem> fetchAptPriceDetails(String houseManageNo, String pblancNo) {
        ApplyhomePriceDetailResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/getAPTLttotPblancMdl")
                        .queryParam("page", 1)
                        .queryParam("perPage", 100)
                        .queryParam("cond[HOUSE_MANAGE_NO::EQ]", houseManageNo)
                        .queryParam("cond[PBLANC_NO::EQ]", pblancNo)
                        .queryParam("serviceKey", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(ApplyhomePriceDetailResponse.class)
                .block();

        return response != null ? response.getData() : Collections.emptyList();
    }

    /**
     * 잔여세대 분양가 상세 조회
     */
    private List<ApplyhomePriceDetailItem> fetchRemainingPriceDetails(String houseManageNo, String pblancNo) {
        ApplyhomePriceDetailResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/getRemndrLttotPblancMdl")
                        .queryParam("page", 1)
                        .queryParam("perPage", 100)
                        .queryParam("cond[HOUSE_MANAGE_NO::EQ]", houseManageNo)
                        .queryParam("cond[PBLANC_NO::EQ]", pblancNo)
                        .queryParam("serviceKey", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(ApplyhomePriceDetailResponse.class)
                .block();

        return response != null ? response.getData() : Collections.emptyList();
    }

    /**
     * 임의공급 분양가 상세 조회
     */
    private List<ApplyhomePriceDetailItem> fetchArbitraryPriceDetails(String houseManageNo, String pblancNo) {
        ApplyhomePriceDetailResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/getArbLttotPblancMdl")
                        .queryParam("page", 1)
                        .queryParam("perPage", 100)
                        .queryParam("cond[HOUSE_MANAGE_NO::EQ]", houseManageNo)
                        .queryParam("cond[PBLANC_NO::EQ]", pblancNo)
                        .queryParam("serviceKey", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(ApplyhomePriceDetailResponse.class)
                .block();

        return response != null ? response.getData() : Collections.emptyList();
    }

    private String nullToEmpty(String value) {
        return value != null ? value : "";
    }
}
