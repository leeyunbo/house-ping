package com.yunbok.houseping.support.dto;

import com.yunbok.houseping.core.domain.RealTransaction;
import com.yunbok.houseping.core.domain.Subscription;
import com.yunbok.houseping.core.domain.SubscriptionPrice;
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

    /**
     * SEO 페이지 설명
     */
    public String getPageDescription() {
        StringBuilder desc = new StringBuilder();
        desc.append(subscription.getHouseName()).append(" 청약 분석 - ").append(subscription.getArea());
        if (subscription.getTotalSupplyCount() != null) {
            desc.append(", ").append(subscription.getTotalSupplyCount()).append("세대");
        }
        if (subscription.getReceiptStartDate() != null) {
            desc.append(", 접수기간 ").append(subscription.getReceiptStartDate());
            if (subscription.getReceiptEndDate() != null) {
                desc.append("~").append(subscription.getReceiptEndDate());
            }
        }
        desc.append(". 분양가, 주변 시세 비교 분석");
        return desc.toString();
    }
}
