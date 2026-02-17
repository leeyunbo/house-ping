package com.yunbok.houseping.support.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class CompetitionRateDetailRow {
    private final String houseType;
    private final String residenceArea;
    private final Integer rank;
    private final Integer supplyCount;
    private final Integer requestCount;
    private final BigDecimal competitionRate;
}
