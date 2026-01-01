package com.yunbok.houseping.infrastructure.adapter.inbound.admin;

import java.time.LocalDate;

/**
 * 검색 파라미터 묶음.
 */
public record AdminSubscriptionSearchCriteria(
        String keyword,
        String area,
        String source,
        LocalDate startDate,
        LocalDate endDate,
        int page,
        int size
) {

    public AdminSubscriptionSearchCriteria {
        page = Math.max(page, 0);
        size = size <= 0 ? 20 : Math.min(size, 100);
    }
}
