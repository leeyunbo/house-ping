package com.yunbok.houseping.core.service.subscription;

import com.yunbok.houseping.adapter.persistence.RealTransactionQueryAdapter;
import com.yunbok.houseping.adapter.persistence.SubscriptionPriceQueryAdapter;
import com.yunbok.houseping.core.domain.RealTransaction;
import com.yunbok.houseping.core.domain.Subscription;
import com.yunbok.houseping.core.domain.SubscriptionPrice;
import com.yunbok.houseping.support.dto.PriceBadge;
import com.yunbok.houseping.support.util.AddressHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PriceBadgeCalculator {

    private static final BigDecimal AREA_TOLERANCE = new BigDecimal("5");

    private final SubscriptionPriceQueryAdapter subscriptionPriceQueryPort;
    private final RealTransactionQueryAdapter realTransactionQueryPort;
    private final AddressHelper addressHelper;
    private final HouseTypeComparisonBuilder comparisonBuilder;

    public PriceBadge computePriceBadge(Subscription subscription) {
        if (subscription.getSource() != null && subscription.getSource().toUpperCase().contains("LH")) {
            return PriceBadge.UNKNOWN;
        }
        if (subscription.getHouseManageNo() == null || subscription.getAddress() == null) {
            return PriceBadge.UNKNOWN;
        }

        List<SubscriptionPrice> prices = subscriptionPriceQueryPort.findByHouseManageNo(subscription.getHouseManageNo());
        if (prices.isEmpty()) {
            return PriceBadge.UNKNOWN;
        }

        SubscriptionPrice representative = selectRepresentativePrice(prices);
        if (representative == null || representative.getTopAmount() == null) {
            return PriceBadge.UNKNOWN;
        }

        BigDecimal area = comparisonBuilder.extractAreaFromHouseType(representative.getHouseType());
        if (area == null) {
            return PriceBadge.UNKNOWN;
        }

        String lawdCd = addressHelper.extractLawdCd(subscription.getAddress());
        String dongName = addressHelper.extractDongName(subscription.getAddress());
        if (lawdCd == null) {
            return PriceBadge.UNKNOWN;
        }

        List<RealTransaction> allTransactions = realTransactionQueryPort.findByLawdCd(lawdCd);
        List<RealTransaction> dongTransactions = addressHelper.filterByDongName(allTransactions, dongName);

        int newBuildYearThreshold = LocalDate.now().getYear() - 4;
        List<RealTransaction> newBuildTx = dongTransactions.stream()
                .filter(t -> t.getBuildYear() != null && t.getBuildYear() >= newBuildYearThreshold)
                .toList();
        if (newBuildTx.isEmpty()) {
            return PriceBadge.UNKNOWN;
        }

        BigDecimal minArea = area.subtract(AREA_TOLERANCE);
        BigDecimal maxArea = area.add(AREA_TOLERANCE);
        List<Long> similarAmounts = newBuildTx.stream()
                .filter(t -> t.getExclusiveArea() != null)
                .filter(t -> t.getExclusiveArea().compareTo(minArea) >= 0
                          && t.getExclusiveArea().compareTo(maxArea) <= 0)
                .map(RealTransaction::getDealAmount)
                .sorted()
                .toList();

        if (similarAmounts.isEmpty()) {
            return PriceBadge.UNKNOWN;
        }

        long median;
        int size = similarAmounts.size();
        if (size % 2 == 0) {
            median = (similarAmounts.get(size / 2 - 1) + similarAmounts.get(size / 2)) / 2;
        } else {
            median = similarAmounts.get(size / 2);
        }

        long supplyPrice = representative.getTopAmount();
        if (supplyPrice < median * 0.95) {
            return PriceBadge.CHEAP;
        } else if (supplyPrice > median * 1.05) {
            return PriceBadge.EXPENSIVE;
        } else {
            return PriceBadge.UNKNOWN;
        }
    }

    public SubscriptionPrice selectRepresentativePrice(List<SubscriptionPrice> prices) {
        BigDecimal target84 = new BigDecimal("84");

        SubscriptionPrice closest84 = prices.stream()
                .filter(p -> comparisonBuilder.extractAreaFromHouseType(p.getHouseType()) != null)
                .filter(p -> {
                    BigDecimal area = comparisonBuilder.extractAreaFromHouseType(p.getHouseType());
                    return area.subtract(target84).abs().compareTo(AREA_TOLERANCE) <= 0;
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
