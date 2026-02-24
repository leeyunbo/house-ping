package com.yunbok.houseping.support.dto;

import com.yunbok.houseping.support.util.PriceFormatter;
import lombok.Builder;
import lombok.Getter;

/**
 * 시장 분석 결과 도메인 모델
 */
@Getter
@Builder
public class MarketAnalysis {

    private final int transactionCount;
    private final Long averageAmount;
    private final Long maxAmount;
    private final Long minAmount;
    private final Long averagePricePerPyeong;

    /**
     * 거래 데이터 존재 여부
     */
    public boolean hasData() {
        return transactionCount > 0;
    }

    /**
     * 평균 금액 포맷
     */
    public String getAverageAmountFormatted() {
        if (averageAmount == null) return "-";
        return PriceFormatter.format(averageAmount);
    }

    /**
     * 최고가 포맷
     */
    public String getMaxAmountFormatted() {
        if (maxAmount == null) return "-";
        return PriceFormatter.format(maxAmount);
    }

    /**
     * 최저가 포맷
     */
    public String getMinAmountFormatted() {
        if (minAmount == null) return "-";
        return PriceFormatter.format(minAmount);
    }

    /**
     * 평균 평당가 포맷
     */
    public String getAveragePricePerPyeongFormatted() {
        if (averagePricePerPyeong == null) return "-";
        return PriceFormatter.format(averagePricePerPyeong) + "/평";
    }
}
