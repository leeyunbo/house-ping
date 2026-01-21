package com.yunbok.houseping.adapter.in.web.admin;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AdminSubscriptionDto(
        Long id,
        String source,
        String houseName,
        String houseType,
        String area,
        LocalDate announceDate,
        LocalDate receiptStartDate,
        LocalDate receiptEndDate,
        LocalDate winnerAnnounceDate,
        String detailUrl,
        String homepageUrl,
        String contact,
        Integer totalSupplyCount,
        LocalDateTime collectedAt,
        LocalDateTime createdAt
) {
}
