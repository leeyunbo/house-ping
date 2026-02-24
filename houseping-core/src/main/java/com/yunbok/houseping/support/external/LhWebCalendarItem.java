package com.yunbok.houseping.support.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LhWebCalendarItem(
    @JsonProperty("panNm") String projectName,
    @JsonProperty("cnpCdNm") String areaName,
    @JsonProperty("uppAisTpCd") String houseTypeCode,
    @JsonProperty("panNtStDt") String announceDate,
    @JsonProperty("acpStDttm") String receiptStartDate,
    @JsonProperty("acpEdDttm") String receiptEndDate,
    @JsonProperty("dtlUrl") String detailUrl,
    @JsonProperty("panSs") String status
) {}
