package com.yunbok.houseping.core.service.admin.dto;

import java.time.LocalDate;

public record AdminSubscriptionSearchCriteria(
        String keyword,
        String area,
        String houseType,
        String source,
        LocalDate startDate,
        LocalDate endDate,
        Integer page,
        Integer size
) {
    public AdminSubscriptionSearchCriteria {
        page = page == null ? 0 : Math.max(page, 0);
        size = size == null || size <= 0 ? 20 : Math.min(size, 100);
    }
}
