package com.yunbok.houseping.adapter.out.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * LH API 응답 DTO
 * LH API는 배열 형태로 응답하며, 두 번째 요소에 dsList가 포함됨
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LhApiResponse(
    @JsonProperty("dsList") List<LhApiItem> items
) {
    public List<LhApiItem> getItems() {
        return items != null ? items : List.of();
    }
}
