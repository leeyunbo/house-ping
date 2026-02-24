package com.yunbok.houseping.support.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LhWebCalendarResponse(
    @JsonProperty("panList") List<LhWebCalendarItem> items
) {
    public List<LhWebCalendarItem> getItems() {
        return items != null ? items : List.of();
    }
}
