package com.yunbok.houseping.support.dto;

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
        return formatPrice(averageAmount);
    }

    /**
     * 최고가 포맷
     */
    public String getMaxAmountFormatted() {
        if (maxAmount == null) return "-";
        return formatPrice(maxAmount);
    }

    /**
     * 최저가 포맷
     */
    public String getMinAmountFormatted() {
        if (minAmount == null) return "-";
        return formatPrice(minAmount);
    }

    /**
     * 평균 평당가 포맷
     */
    public String getAveragePricePerPyeongFormatted() {
        if (averagePricePerPyeong == null) return "-";
        return formatPrice(averagePricePerPyeong) + "/평";
    }

    private String formatPrice(long amount) {
        if (amount >= 10000) {
            long uk = amount / 10000;
            long rest = amount % 10000;
            if (rest == 0) {
                return uk + "억";
            }
            return uk + "억 " + String.format("%,d", rest) + "만";
        }
        return String.format("%,d", amount) + "만";
    }
}
