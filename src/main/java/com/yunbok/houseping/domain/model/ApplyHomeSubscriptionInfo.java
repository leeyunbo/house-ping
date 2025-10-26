package com.yunbok.houseping.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Objects;

@Getter
@Builder
public class ApplyHomeSubscriptionInfo implements SubscriptionInfo {

    private String houseManageNo;      // 주택관리번호 (중복제거 키)
    private String pblancNo;           // 공고번호
    private String houseName;          // 주택명
    private String houseType;          // 주택구분 (APT, 무순위, 민간사전청약 등)
    private String area;               // 지역 (서울, 경기, 인천)
    private LocalDate announceDate;    // 모집공고일
    private LocalDate receiptStartDate; // 청약접수시작일
    private LocalDate receiptEndDate;   // 청약접수종료일
    private LocalDate winnerAnnounceDate; // 당첨자발표일
    private String homepageUrl;        // 홈페이지
    private String detailUrl;          // 청약홈 상세페이지 URL
    private String contact;            // 문의처
    private Integer totalSupplyCount;  // 공급규모

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ApplyHomeSubscriptionInfo that = (ApplyHomeSubscriptionInfo) o;
        return Objects.equals(houseManageNo, that.houseManageNo);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(houseManageNo);
    }
}

