package com.yunbok.houseping.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Objects;

@Getter
@Builder
public class LhSubscriptionInfo implements SubscriptionInfo{

    private String houseName;          // 주택명

    private String houseType;          // 주택구분 (APT, 무순위, 민간사전청약 등)

    @JsonProperty("CNP_CD_NM")
    private String area;               // 지역 (서울, 경기, 인천)

    @JsonProperty("PAN_NT_ST_DT")
    private LocalDate announceDate; // 공고일

    @JsonProperty("CLSG_DT")
    private LocalDate receiptEndDate;   // 청약접수종료일

    @JsonProperty("DTL_URL")
    private String detailUrl;          // 청약홈 상세페이지 URL

    @JsonProperty("PAN_SS")
    private String subscriptionStatus; // 공고상태

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        LhSubscriptionInfo that = (LhSubscriptionInfo) o;
        return Objects.equals(detailUrl, that.detailUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(detailUrl);
    }
}
