package com.yunbok.houseping.support.dto;

import com.yunbok.houseping.adapter.dto.SubscriptionInfo;

import java.time.LocalDate;

public record SubscriptionDto(
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
        Integer totalSupplyCount
) {
    public static SubscriptionDto from(SubscriptionInfo info) {
        return new SubscriptionDto(
                info.getHouseName(),
                info.getHouseType(),
                info.getArea(),
                info.getAnnounceDate(),
                info.getReceiptStartDate(),
                info.getReceiptEndDate(),
                info.getWinnerAnnounceDate(),
                info.getDetailUrl(),
                info.getHomepageUrl(),
                info.getContact(),
                info.getTotalSupplyCount()
        );
    }
}
