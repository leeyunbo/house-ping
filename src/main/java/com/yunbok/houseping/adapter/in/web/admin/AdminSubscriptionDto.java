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
        LocalDateTime createdAt,
        boolean notificationEnabled,
        boolean expired,
        String address,
        String zipCode,
        String houseManageNo
) {
    public AdminSubscriptionDto(
            Long id, String source, String houseName, String houseType, String area,
            LocalDate announceDate, LocalDate receiptStartDate, LocalDate receiptEndDate,
            LocalDate winnerAnnounceDate, String detailUrl, String homepageUrl, String contact,
            Integer totalSupplyCount, LocalDateTime collectedAt, LocalDateTime createdAt,
            String address, String zipCode, String houseManageNo) {
        this(id, source, houseName, houseType, area, announceDate, receiptStartDate, receiptEndDate,
             winnerAnnounceDate, detailUrl, homepageUrl, contact, totalSupplyCount, collectedAt, createdAt,
             false, receiptEndDate != null && receiptEndDate.isBefore(LocalDate.now()),
             address, zipCode, houseManageNo);
    }
}
