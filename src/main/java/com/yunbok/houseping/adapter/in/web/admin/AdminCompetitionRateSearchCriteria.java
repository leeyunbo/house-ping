package com.yunbok.houseping.adapter.in.web.admin;

import java.math.BigDecimal;

public record AdminCompetitionRateSearchCriteria(
        String keyword,
        String houseType,
        Integer rank,
        String residenceArea,
        BigDecimal minRate,
        BigDecimal maxRate,
        Integer page,
        Integer size
) {
    public AdminCompetitionRateSearchCriteria {
        page = page == null ? 0 : Math.max(page, 0);
        size = size == null || size <= 0 ? 20 : Math.min(size, 100);
    }
}
