package com.yunbok.houseping.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@EqualsAndHashCode(of = "detailUrl")
public class LhSubscriptionInfo implements SubscriptionInfo {

    private String houseName;
    private String houseType;
    private String area;
    private LocalDate announceDate;
    private LocalDate receiptEndDate;
    private String detailUrl;
    private String subscriptionStatus;

    @Override
    public String getDisplayMessage() {
        return String.format("""
                        %s [%s] %s
                        📅 공고일: %s
                        📅 청약접수종료일: %s
                        🔗 %s
                        """,
                houseType, area, houseName,
                announceDate, receiptEndDate,
                detailUrl
        );

    }

    @Override
    public String getSimpleDisplayMessage() {
        return String.format("[%s] %s\n", area, houseName);
    }
}
