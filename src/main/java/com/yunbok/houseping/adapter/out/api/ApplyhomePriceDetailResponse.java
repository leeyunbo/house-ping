package com.yunbok.houseping.adapter.out.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collections;
import java.util.List;

/**
 * 청약Home API 주택형별 분양정보 (분양가 상세) 응답 DTO
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ApplyhomePriceDetailResponse(
    Integer currentCount,
    List<ApplyhomePriceDetailItem> data,
    Integer matchCount,
    Integer page,
    Integer perPage,
    Integer totalCount
) {
    public List<ApplyhomePriceDetailItem> getData() {
        return data != null ? data : Collections.emptyList();
    }
}
