package com.yunbok.houseping.domain.model;

import java.time.LocalDate;

/**
 * 알림 발송 대상 도메인 모델
 */
public record NotificationTarget(
        Long notificationId,
        Long subscriptionId,
        String houseName,
        String area,
        LocalDate receiptStartDate,
        LocalDate receiptEndDate,
        Integer totalSupplyCount,
        String detailUrl
) {
}
