package com.yunbok.houseping.infrastructure.dto;

import com.yunbok.houseping.core.domain.Subscription;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@EqualsAndHashCode(of = "houseManageNo")
public class ApplyHomeSubscriptionInfo {

    private String houseManageNo;
    private String pblancNo;
    private String houseName;
    private String houseType;
    private String area;
    private LocalDate announceDate;
    private LocalDate receiptStartDate;
    private LocalDate receiptEndDate;
    private LocalDate winnerAnnounceDate;
    private String homepageUrl;
    private String detailUrl;
    private String contact;
    private Integer totalSupplyCount;
    private String address;
    private String zipCode;

    public Subscription toSubscription() {
        return Subscription.builder()
                .houseManageNo(houseManageNo)
                .pblancNo(pblancNo)
                .houseName(houseName)
                .houseType(houseType)
                .area(area)
                .announceDate(announceDate)
                .receiptStartDate(receiptStartDate)
                .receiptEndDate(receiptEndDate)
                .winnerAnnounceDate(winnerAnnounceDate)
                .homepageUrl(homepageUrl)
                .detailUrl(detailUrl)
                .contact(contact)
                .totalSupplyCount(totalSupplyCount)
                .address(address)
                .zipCode(zipCode)
                .build();
    }
}
