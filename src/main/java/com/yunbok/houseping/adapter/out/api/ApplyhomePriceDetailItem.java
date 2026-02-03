package com.yunbok.houseping.adapter.out.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * 청약Home API 주택형별 분양정보 (분양가 상세) 응답 항목 DTO
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ApplyhomePriceDetailItem(
    @JsonProperty("HOUSE_MANAGE_NO") String houseManageNo,
    @JsonProperty("PBLANC_NO") String pblancNo,
    @JsonProperty("MODEL_NO") String modelNo,
    @JsonProperty("HOUSE_TY") String houseType,
    @JsonProperty("SUPLY_AR") BigDecimal supplyArea,
    @JsonProperty("SUPLY_HSHLDCO") Integer supplyCount,
    @JsonProperty("SPSPLY_HSHLDCO") Integer specialSupplyCount,
    @JsonProperty("MNYCH_HSHLDCO") Integer multiChildCount,
    @JsonProperty("NWWDS_HSHLDCO") Integer newlywedCount,
    @JsonProperty("LFE_FRST_HSHLDCO") Integer lifeFirstCount,
    @JsonProperty("OLD_PARNTS_SUPORT_HSHLDCO") Integer oldParentsSupportCount,
    @JsonProperty("INSTT_RECOMEND_HSHLDCO") Integer institutionRecommendCount,
    @JsonProperty("LTTOT_TOP_AMOUNT") Long topAmount
) {
    /**
     * 평당 가격 계산 (만원/평)
     */
    public Long getPricePerPyeong() {
        if (topAmount == null || supplyArea == null || supplyArea.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        // 1평 = 3.3058 제곱미터
        BigDecimal pyeong = supplyArea.divide(BigDecimal.valueOf(3.3058), 2, java.math.RoundingMode.HALF_UP);
        if (pyeong.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return BigDecimal.valueOf(topAmount).divide(pyeong, 0, java.math.RoundingMode.HALF_UP).longValue();
    }
}
