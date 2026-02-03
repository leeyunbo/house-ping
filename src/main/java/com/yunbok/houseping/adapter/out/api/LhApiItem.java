package com.yunbok.houseping.adapter.out.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * LH API 응답 항목 DTO
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LhApiItem(
    @JsonProperty("PAN_NM") String projectName,
    @JsonProperty("CNP_CD_NM") String areaName,
    @JsonProperty("UPP_AIS_TP_CD") String houseTypeCode,
    @JsonProperty("PAN_NT_ST_DT") String announceDate,
    @JsonProperty("CLSG_DT") String closeDate,
    @JsonProperty("PAN_SS") String status,
    @JsonProperty("DTL_URL") String detailUrl
) {}
