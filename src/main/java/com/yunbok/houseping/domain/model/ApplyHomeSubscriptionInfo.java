package com.yunbok.houseping.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@EqualsAndHashCode(of = "houseManageNo")
public class ApplyHomeSubscriptionInfo implements SubscriptionInfo {

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

    @Override
    public String getDisplayMessage() {
        return String.format("""
                        [%s] %s
                        📅 접수: %s ~ %s
                        🏆 발표: %s
                        🏠 세대수: %d세대
                        🔗 %s
                        """,
                area, houseName,
                receiptStartDate, receiptEndDate,
                winnerAnnounceDate,
                totalSupplyCount,
                detailUrl
        );
    }

    @Override
    public String getSimpleDisplayMessage() {
        return String.format("[%s] %s\n", area, houseName);
    }
}
