package com.yunbok.houseping.core.service.subscription;

import com.yunbok.houseping.support.dto.MarketAnalysis;
import com.yunbok.houseping.core.domain.RealTransaction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * 시장 분석 담당
 */
@Component
public class MarketAnalyzer {

    /**
     * 거래 내역 기반 시장 분석
     */
    public MarketAnalysis analyze(List<RealTransaction> transactions) {
        if (transactions.isEmpty()) {
            return null;
        }

        long avgAmount = (long) transactions.stream()
                .mapToLong(RealTransaction::getDealAmount)
                .average()
                .orElse(0);

        long avgPricePerPyeong = (long) transactions.stream()
                .map(RealTransaction::getPricePerPyeong)
                .filter(Objects::nonNull)
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);

        long maxAmount = transactions.stream()
                .mapToLong(RealTransaction::getDealAmount)
                .max()
                .orElse(0);

        long minAmount = transactions.stream()
                .mapToLong(RealTransaction::getDealAmount)
                .min()
                .orElse(0);

        return MarketAnalysis.builder()
                .averageAmount(avgAmount)
                .averagePricePerPyeong(avgPricePerPyeong)
                .maxAmount(maxAmount)
                .minAmount(minAmount)
                .transactionCount(transactions.size())
                .build();
    }
}
