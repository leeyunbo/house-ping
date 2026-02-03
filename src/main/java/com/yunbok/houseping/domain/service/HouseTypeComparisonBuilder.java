package com.yunbok.houseping.domain.service;

import com.yunbok.houseping.domain.model.HouseTypeComparison;
import com.yunbok.houseping.domain.model.RealTransaction;
import com.yunbok.houseping.domain.model.SubscriptionPrice;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 주택형별 시세 비교 생성 담당
 */
@Component
public class HouseTypeComparisonBuilder {

    private static final BigDecimal AREA_TOLERANCE = new BigDecimal("5");
    private static final int MAX_SIMILAR_TRANSACTIONS = 5;

    /**
     * 주택형별 시세 비교 생성
     */
    public List<HouseTypeComparison> build(List<SubscriptionPrice> prices, List<RealTransaction> transactions) {
        if (prices.isEmpty()) {
            return Collections.emptyList();
        }

        List<HouseTypeComparison> comparisons = new ArrayList<>();

        for (SubscriptionPrice price : prices) {
            BigDecimal exclusiveArea = extractAreaFromHouseType(price.getHouseType());
            if (exclusiveArea == null) continue;

            List<RealTransaction> similarAreaTx = findSimilarAreaTransactions(transactions, exclusiveArea);

            Long marketPrice = calculateAveragePrice(similarAreaTx);
            Long estimatedProfit = calculateProfit(marketPrice, price.getTopAmount());

            comparisons.add(HouseTypeComparison.builder()
                    .houseType(price.getHouseType())
                    .supplyArea(exclusiveArea)
                    .supplyPrice(price.getTopAmount())
                    .marketPrice(marketPrice)
                    .estimatedProfit(estimatedProfit)
                    .similarTransactions(similarAreaTx)
                    .build());
        }

        return comparisons;
    }

    /**
     * 유사 면적 거래 찾기 (±5㎡, 최근 5건)
     */
    private List<RealTransaction> findSimilarAreaTransactions(List<RealTransaction> transactions, BigDecimal targetArea) {
        BigDecimal minArea = targetArea.subtract(AREA_TOLERANCE);
        BigDecimal maxArea = targetArea.add(AREA_TOLERANCE);

        return transactions.stream()
                .filter(t -> t.getExclusiveArea() != null)
                .filter(t -> t.getExclusiveArea().compareTo(minArea) >= 0
                          && t.getExclusiveArea().compareTo(maxArea) <= 0)
                .sorted((a, b) -> b.getDealDate().compareTo(a.getDealDate()))
                .limit(MAX_SIMILAR_TRANSACTIONS)
                .toList();
    }

    /**
     * 평균 시세 계산
     */
    private Long calculateAveragePrice(List<RealTransaction> transactions) {
        if (transactions.isEmpty()) {
            return null;
        }
        return (long) transactions.stream()
                .mapToLong(RealTransaction::getDealAmount)
                .average()
                .orElse(0);
    }

    /**
     * 예상 차익 계산
     */
    private Long calculateProfit(Long marketPrice, Long supplyPrice) {
        if (marketPrice == null || supplyPrice == null) {
            return null;
        }
        return marketPrice - supplyPrice;
    }

    /**
     * 주택형에서 면적 추출 (정수 부분만)
     */
    BigDecimal extractAreaFromHouseType(String houseType) {
        if (houseType == null) return null;
        try {
            String numStr = houseType.replaceAll("[^0-9].*", "").replaceFirst("^0+", "");
            if (numStr.isEmpty()) return null;
            return new BigDecimal(numStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
