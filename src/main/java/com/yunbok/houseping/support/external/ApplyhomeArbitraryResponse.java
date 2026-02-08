package com.yunbok.houseping.support.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collections;
import java.util.List;

/**
 * 청약Home API 임의공급 응답 DTO
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ApplyhomeArbitraryResponse(
    Integer currentCount,
    List<ApplyhomeArbitraryItem> data,
    Integer matchCount,
    Integer page,
    Integer perPage,
    Integer totalCount
) {
    public List<ApplyhomeArbitraryItem> getData() {
        return data != null ? data : Collections.emptyList();
    }
}
