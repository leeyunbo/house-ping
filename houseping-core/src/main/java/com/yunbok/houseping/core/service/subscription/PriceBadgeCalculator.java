package com.yunbok.houseping.core.service.subscription;

import com.yunbok.houseping.core.port.RealTransactionPersistencePort;
import com.yunbok.houseping.core.port.SubscriptionPricePersistencePort;
import com.yunbok.houseping.core.domain.RealTransaction;
import com.yunbok.houseping.core.domain.Subscription;
import com.yunbok.houseping.core.domain.SubscriptionPrice;
import com.yunbok.houseping.support.dto.PriceBadge;
import com.yunbok.houseping.support.util.AddressHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PriceBadgeCalculator {

    private final SubscriptionPricePersistencePort subscriptionPriceQueryPort;
    private final RealTransactionPersistencePort realTransactionQueryPort;
    private final AddressHelper addressHelper;
    private final HouseTypeComparisonBuilder comparisonBuilder;

    /**
     * 청약 분양가와 주변 시세를 비교하여 가격 배지를 산출한다.
     *
     * <ol>
     *   <li>LH 청약이거나 관리번호·주소가 없으면 {@code UNKNOWN}</li>
     *   <li>대표 평형(84㎡ 기준, ±5㎡ 허용)의 분양가를 선택</li>
     *   <li>같은 동 내 최근 3년 이내 신축 실거래가 중 유사 면적(±5㎡)의 중앙값을 시세로 산출</li>
     *   <li>분양가 &lt; 시세의 95% → {@code CHEAP}</li>
     *   <li>분양가 &gt; 시세의 105% → {@code EXPENSIVE}</li>
     *   <li>그 외 → {@code UNKNOWN}</li>
     * </ol>
     */
    public PriceBadge computePriceBadge(Subscription subscription) {
        if (subscription.getSource() != null && subscription.getSource().toUpperCase().contains("LH")) {
            return PriceBadge.UNKNOWN;
        }
        if (subscription.getHouseManageNo() == null || subscription.getAddress() == null) {
            return PriceBadge.UNKNOWN;
        }

        BigDecimal area = findRepresentativeArea(subscription);
        if (area == null) {
            return PriceBadge.UNKNOWN;
        }

        String lawdCd = addressHelper.extractLawdCd(subscription.getAddress());
        String dongName = addressHelper.extractDongName(subscription.getAddress());
        if (lawdCd == null) {
            return PriceBadge.UNKNOWN;
        }

        List<RealTransaction> newBuildTx = findNewBuildTransactions(lawdCd, dongName);
        if (newBuildTx.isEmpty()) {
            return PriceBadge.UNKNOWN;
        }

        Long median = calculateMedianPrice(newBuildTx, area);
        if (median == null) {
            return PriceBadge.UNKNOWN;
        }

        long supplyPrice = selectRepresentativePrice(
                subscriptionPriceQueryPort.findByHouseManageNo(subscription.getHouseManageNo())
        ).getTopAmount();
        return determineBadge(supplyPrice, median);
    }

    private BigDecimal findRepresentativeArea(Subscription subscription) {
        List<SubscriptionPrice> prices = subscriptionPriceQueryPort.findByHouseManageNo(subscription.getHouseManageNo());
        if (prices.isEmpty()) {
            return null;
        }
        SubscriptionPrice representative = selectRepresentativePrice(prices);
        if (representative == null || representative.getTopAmount() == null) {
            return null;
        }
        return comparisonBuilder.extractAreaFromHouseType(representative.getHouseType());
    }

    private List<RealTransaction> findNewBuildTransactions(String lawdCd, String dongName) {
        List<RealTransaction> allTransactions = realTransactionQueryPort.findByLawdCd(lawdCd);
        List<RealTransaction> dongTransactions = addressHelper.filterByDongName(allTransactions, dongName);
        int threshold = HouseTypeComparisonBuilder.newBuildYearThreshold();
        return dongTransactions.stream()
                .filter(t -> t.getBuildYear() != null && t.getBuildYear() >= threshold)
                .toList();
    }

    private Long calculateMedianPrice(List<RealTransaction> transactions, BigDecimal area) {
        BigDecimal minArea = area.subtract(HouseTypeComparisonBuilder.AREA_TOLERANCE);
        BigDecimal maxArea = area.add(HouseTypeComparisonBuilder.AREA_TOLERANCE);
        List<Long> amounts = transactions.stream()
                .filter(t -> t.getExclusiveArea() != null)
                .filter(t -> t.getExclusiveArea().compareTo(minArea) >= 0
                          && t.getExclusiveArea().compareTo(maxArea) <= 0)
                .map(RealTransaction::getDealAmount)
                .sorted()
                .toList();
        if (amounts.isEmpty()) {
            return null;
        }
        int size = amounts.size();
        if (size % 2 == 0) {
            return (amounts.get(size / 2 - 1) + amounts.get(size / 2)) / 2;
        }
        return amounts.get(size / 2);
    }

    private PriceBadge determineBadge(long supplyPrice, long median) {
        if (supplyPrice < median * 0.95) {
            return PriceBadge.CHEAP;
        } else if (supplyPrice > median * 1.05) {
            return PriceBadge.EXPENSIVE;
        }
        return PriceBadge.UNKNOWN;
    }

    public SubscriptionPrice selectRepresentativePrice(List<SubscriptionPrice> prices) {
        BigDecimal target84 = new BigDecimal("84");

        SubscriptionPrice closest84 = prices.stream()
                .filter(p -> comparisonBuilder.extractAreaFromHouseType(p.getHouseType()) != null)
                .filter(p -> {
                    BigDecimal area = comparisonBuilder.extractAreaFromHouseType(p.getHouseType());
                    return area.subtract(target84).abs().compareTo(HouseTypeComparisonBuilder.AREA_TOLERANCE) <= 0;
                })
                .min(Comparator.comparing(p -> comparisonBuilder.extractAreaFromHouseType(p.getHouseType()).subtract(target84).abs()))
                .orElse(null);

        if (closest84 != null) {
            return closest84;
        }

        return prices.stream()
                .max(Comparator.comparingInt(SubscriptionPrice::getTotalSupplyCount))
                .orElse(null);
    }
}
