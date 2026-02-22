package com.yunbok.houseping.core.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 청약 정보 도메인 모델
 * Entity가 아닌 순수 도메인 객체
 */
@Getter
@Builder
public class Subscription {

    private final Long id;
    private final String source;
    private final String houseManageNo;
    private final String pblancNo;
    private final String houseName;
    private final String houseType;
    private final String area;
    private final LocalDate announceDate;
    private final LocalDate receiptStartDate;
    private final LocalDate receiptEndDate;
    private final LocalDate winnerAnnounceDate;
    private final String detailUrl;
    private final String homepageUrl;
    private final String contact;
    private final Integer totalSupplyCount;
    private final String address;
    private final String zipCode;

    public String getDisplayMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(area).append("] ").append(houseName);
        if (totalSupplyCount != null) {
            sb.append(" (").append(totalSupplyCount).append("세대)");
        }
        sb.append("\n");
        sb.append("접수: ").append(receiptStartDate);
        if (receiptEndDate != null) {
            sb.append(" ~ ").append(receiptEndDate);
        }
        if (detailUrl != null) {
            sb.append("\n").append(detailUrl);
        }
        return sb.toString();
    }

    public String getSimpleDisplayMessage() {
        return String.format("[%s] %s", area, houseName);
    }

    public boolean isReceiptInProgress() {
        LocalDate today = LocalDate.now();
        LocalDate start = getReceiptStartDate();
        LocalDate end = getReceiptEndDate();
        return start != null && !today.isBefore(start)
            && (end == null || !today.isAfter(end));
    }

    public boolean isUpcoming() {
        LocalDate today = LocalDate.now();
        LocalDate start = getReceiptStartDate();
        return start != null && today.isBefore(start);
    }

    public boolean isExpired() {
        LocalDate today = LocalDate.now();
        LocalDate end = getReceiptEndDate();
        return end != null && today.isAfter(end);
    }

    public boolean isValid() {
        return getHouseName() != null && !getHouseName().isBlank()
            && getArea() != null && !getArea().isBlank();
    }

    public boolean isLargeSupply() {
        Integer count = getTotalSupplyCount();
        return count != null && count >= 100;
    }

    /**
     * 청약 상태 반환
     */
    public SubscriptionStatus getStatus() {
        if (isReceiptInProgress()) {
            return SubscriptionStatus.ACTIVE;
        } else if (isUpcoming()) {
            return SubscriptionStatus.UPCOMING;
        } else {
            return SubscriptionStatus.CLOSED;
        }
    }

    /**
     * 상태 라벨 반환
     */
    public String getStatusLabel() {
        return getStatus().getLabel();
    }
}
