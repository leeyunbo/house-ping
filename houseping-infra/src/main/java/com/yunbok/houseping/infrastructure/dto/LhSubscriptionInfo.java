package com.yunbok.houseping.infrastructure.dto;

import com.yunbok.houseping.core.domain.Subscription;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@EqualsAndHashCode(of = "detailUrl")
public class LhSubscriptionInfo {

    private String houseName;
    private String houseType;
    private String area;
    private LocalDate announceDate;
    private LocalDate receiptStartDate;
    private LocalDate receiptEndDate;
    private String detailUrl;
    private String subscriptionStatus;

    public LocalDate getReceiptStartDate() {
        return receiptStartDate != null ? receiptStartDate : announceDate;
    }

    public Subscription toSubscription() {
        return Subscription.builder()
                .houseName(houseName)
                .houseType(houseType)
                .area(area)
                .announceDate(announceDate)
                .receiptStartDate(getReceiptStartDate())
                .receiptEndDate(receiptEndDate)
                .detailUrl(detailUrl)
                .build();
    }
}
