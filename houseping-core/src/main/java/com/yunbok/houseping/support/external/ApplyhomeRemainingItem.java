package com.yunbok.houseping.support.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 청약Home API 잔여세대 응답 항목 DTO
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ApplyhomeRemainingItem(
    @JsonProperty("HOUSE_MANAGE_NO") String houseManageNo,
    @JsonProperty("PBLANC_NO") String pblancNo,
    @JsonProperty("HOUSE_NM") String houseName,
    @JsonProperty("SUBSCRPT_AREA_CODE_NM") String areaName,
    @JsonProperty("RCRIT_PBLANC_DE") String announceDate,
    @JsonProperty("SUBSCRPT_RCEPT_BGNDE") String receiptStartDate,
    @JsonProperty("SUBSCRPT_RCEPT_ENDDE") String receiptEndDate,
    @JsonProperty("PRZWNER_PRESNATN_DE") String winnerAnnounceDate,
    @JsonProperty("TOT_SUPLY_HSHLDCO") Integer totalSupplyCount,
    @JsonProperty("HMPG_ADRES") String homepageUrl,
    @JsonProperty("PBLANC_URL") String detailUrl,
    @JsonProperty("MDHS_TELNO") String contact,
    @JsonProperty("HSSPLY_ADRES") String address,
    @JsonProperty("HSSPLY_ZIP") String zipCode
) {}
