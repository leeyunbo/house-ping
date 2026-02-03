package com.yunbok.houseping.adapter.out.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 경쟁률 API 응답
 */
public record CompetitionRateResponse(
        @JsonProperty("currentCount")
        Integer currentCount,
        @JsonProperty("totalCount")
        Integer totalCount,
        @JsonProperty("data")
        List<CompetitionRateItem> data
) {
    public CompetitionRateResponse {
        if (data == null) {
            data = List.of();
        }
    }
}
