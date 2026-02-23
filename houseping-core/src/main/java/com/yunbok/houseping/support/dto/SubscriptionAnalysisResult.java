package com.yunbok.houseping.support.dto;

import com.yunbok.houseping.core.domain.RealTransaction;
import com.yunbok.houseping.core.domain.Subscription;
import com.yunbok.houseping.core.domain.SubscriptionPrice;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
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
    private final List<CompetitionRateDetailRow> competitionRates;
    private final String dongName;
    private final boolean newBuildBased;

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
     * 경쟁률 정보 존재 여부
     */
    public boolean hasCompetitionRates() {
        return competitionRates != null && !competitionRates.isEmpty();
    }

    /**
     * 경쟁률 리스트 (null-safe)
     */
    public List<CompetitionRateDetailRow> getCompetitionRates() {
        return competitionRates != null ? competitionRates : Collections.emptyList();
    }

    /**
     * 대표 비교 항목 (84㎡ 우선, 없으면 면적 내림차순)
     */
    public HouseTypeComparison getRepresentativeComparison() {
        if (houseTypeComparisons == null || houseTypeComparisons.isEmpty()) {
            return null;
        }
        List<HouseTypeComparison> withProfit = houseTypeComparisons.stream()
                .filter(c -> c.getEstimatedProfit() != null)
                .toList();
        if (withProfit.isEmpty()) {
            return null;
        }
        BigDecimal target = new BigDecimal("84");
        return withProfit.stream()
                .min(Comparator.comparing((HouseTypeComparison c) -> c.getSupplyArea().subtract(target).abs())
                        .thenComparing(c -> c.getSupplyArea().negate()))
                .orElse(null);
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
