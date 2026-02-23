package com.yunbok.houseping.support.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 청약Home API 일반 APT 응답 DTO
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ApplyhomeAptResponse(
    @JsonProperty("currentCount") Integer currentCount,
    @JsonProperty("data") List<ApplyhomeApiItem> data,
    @JsonProperty("matchCount") Integer matchCount,
    @JsonProperty("page") Integer page,
    @JsonProperty("perPage") Integer perPage,
    @JsonProperty("totalCount") Integer totalCount
) {
    public List<ApplyhomeApiItem> getData() {
        return data != null ? data : List.of();
    }
}
