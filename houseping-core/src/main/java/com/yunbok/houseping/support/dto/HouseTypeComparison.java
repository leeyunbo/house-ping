package com.yunbok.houseping.support.dto;

import com.yunbok.houseping.core.domain.RealTransaction;
import com.yunbok.houseping.support.util.PriceFormatter;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 주택형별 시세 비교 도메인 모델
 */
@Getter
@Builder
public class HouseTypeComparison {

    private final String houseType;
    private final BigDecimal supplyArea;
    private final Long supplyPrice;
    private final Long marketPrice;
    private final Long estimatedProfit;
    private final List<RealTransaction> similarTransactions;

    /**
     * 분양가 포맷
     */
    public String getSupplyPriceFormatted() {
        if (supplyPrice == null) return "-";
        return PriceFormatter.format(supplyPrice);
    }

    /**
     * 시세 포맷
     */
    public String getMarketPriceFormatted() {
        if (marketPrice == null) return "거래 없음";
        return PriceFormatter.format(marketPrice);
    }

    /**
     * 예상 차익 포맷
     */
    public String getEstimatedProfitFormatted() {
        if (estimatedProfit == null) return "-";
        String sign = estimatedProfit >= 0 ? "+" : "-";
        return sign + PriceFormatter.format(Math.abs(estimatedProfit));
    }

    /**
     * 차익 여부 (양수면 true)
     */
    public boolean hasProfit() {
        return estimatedProfit != null && estimatedProfit > 0;
    }

    /**
     * 시세 데이터 존재 여부
     */
    public boolean hasMarketData() {
        return marketPrice != null;
    }

    /**
     * 거래 정보 문자열
     */
    public String getTransactionInfo() {
        if (similarTransactions == null || similarTransactions.isEmpty()) {
            return null;
        }
        return "최근 " + similarTransactions.size() + "건 거래 기준";
    }

}
