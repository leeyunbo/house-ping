package com.yunbok.houseping.support.dto;

import com.yunbok.houseping.core.domain.Subscription;

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
    public static SubscriptionDto from(Subscription subscription) {
        return new SubscriptionDto(
                subscription.getHouseName(),
                subscription.getHouseType(),
                subscription.getArea(),
                subscription.getAnnounceDate(),
                subscription.getReceiptStartDate(),
                subscription.getReceiptEndDate(),
                subscription.getWinnerAnnounceDate(),
                subscription.getDetailUrl(),
                subscription.getHomepageUrl(),
                subscription.getContact(),
                subscription.getTotalSupplyCount()
        );
    }
}
