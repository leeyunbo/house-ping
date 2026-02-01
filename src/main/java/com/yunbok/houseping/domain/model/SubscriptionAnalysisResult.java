package com.yunbok.houseping.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 청약 분석 결과 도메인 모델
 */
@Getter
@Builder
public class SubscriptionAnalysisResult {

    private final Subscription subscription;
    private final List<SubscriptionPrice> prices;
    private final MarketAnalysis marketAnalysis;
    private final List<RealTransaction> recentTransactions;
    private final List<HouseTypeComparison> houseTypeComparisons;
    private final String dongName;

    /**
     * 분양가 정보 존재 여부
     */
    public boolean hasPrices() {
        return prices != null && !prices.isEmpty();
    }

    /**
     * 시장 분석 정보 존재 여부
     */
    public boolean hasMarketAnalysis() {
        return marketAnalysis != null && marketAnalysis.hasData();
    }

    /**
     * 주택형별 비교 정보 존재 여부
     */
    public boolean hasHouseTypeComparisons() {
        return houseTypeComparisons != null && !houseTypeComparisons.isEmpty();
    }
}
