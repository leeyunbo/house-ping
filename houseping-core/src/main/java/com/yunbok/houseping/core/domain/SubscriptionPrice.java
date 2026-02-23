package com.yunbok.houseping.core.domain;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 분양가 정보 도메인 모델
 */
@Getter
@Builder
public class SubscriptionPrice {

    private final Long id;
    private final String houseManageNo;
    private final String pblancNo;
    private final String modelNo;
    private final String houseType;
    private final BigDecimal supplyArea;
    private final Integer supplyCount;
    private final Integer specialSupplyCount;
    private final Long topAmount;
    private final Long pricePerPyeong;

    /**
     * 총 공급 세대수 (일반 + 특별)
     */
    public int getTotalSupplyCount() {
        int general = supplyCount != null ? supplyCount : 0;
        int special = specialSupplyCount != null ? specialSupplyCount : 0;
        return general + special;
    }

    /**
     * 분양가 포맷 (억/만원)
     */
    public String getTopAmountFormatted() {
        if (topAmount == null) return "-";
        return formatPrice(topAmount);
    }

    /**
     * 평당가 포맷
     */
    public String getPricePerPyeongFormatted() {
        if (pricePerPyeong == null) return "-";
        return formatPrice(pricePerPyeong) + "/평";
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
