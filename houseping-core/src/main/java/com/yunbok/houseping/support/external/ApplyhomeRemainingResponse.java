package com.yunbok.houseping.support.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 청약Home API 잔여세대 응답 DTO
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ApplyhomeRemainingResponse(
    @JsonProperty("currentCount") Integer currentCount,
    @JsonProperty("data") List<ApplyhomeRemainingItem> data,
    @JsonProperty("matchCount") Integer matchCount,
    @JsonProperty("page") Integer page,
    @JsonProperty("perPage") Integer perPage,
    @JsonProperty("totalCount") Integer totalCount
) {
    public List<ApplyhomeRemainingItem> getData() {
        return data != null ? data : List.of();
    }
}
