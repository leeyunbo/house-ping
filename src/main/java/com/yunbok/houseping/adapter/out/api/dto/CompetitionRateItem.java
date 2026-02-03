package com.yunbok.houseping.adapter.out.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 경쟁률 API 응답 항목
 */
public record CompetitionRateItem(
        @JsonProperty("HOUSE_MANAGE_NO")
        String houseManageNo,

        @JsonProperty("PBLANC_NO")
        String pblancNo,

        @JsonProperty("HOUSE_TY")
        String houseType,

        @JsonProperty("SUPLY_HSHLDCO")
        Integer supplyCount,

        @JsonProperty("REQ_CNT")
        String requestCount,

        @JsonProperty("CMPET_RATE")
        String competitionRate,

        @JsonProperty("RESIDE_SENM")
        String residenceArea,

        @JsonProperty("SUBSCRPT_RANK_CODE")
        Integer rank
) {
}
