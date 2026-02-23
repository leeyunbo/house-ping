package com.yunbok.houseping.core.domain;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 실거래 정보 도메인 모델
 */
@Getter
@Builder
public class RealTransaction {

    private final Long id;
    private final String lawdCd;
    private final String dealYmd;
    private final String aptName;
    private final Long dealAmount;
    private final BigDecimal exclusiveArea;
    private final Integer floor;
    private final Integer buildYear;
    private final LocalDate dealDate;
    private final String dongName;
    private final String jibun;

    /**
     * 평당 가격 계산 (만원/평)
     */
    public Long getPricePerPyeong() {
        if (exclusiveArea == null || exclusiveArea.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        BigDecimal pyeong = exclusiveArea.divide(BigDecimal.valueOf(3.3058), 2, java.math.RoundingMode.HALF_UP);
        return BigDecimal.valueOf(dealAmount).divide(pyeong, 0, java.math.RoundingMode.HALF_UP).longValue();
    }

    /**
     * 거래금액 포맷 (억/만원)
     */
    public String getDealAmountFormatted() {
        if (dealAmount == null) return "-";
        return formatPrice(dealAmount);
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
