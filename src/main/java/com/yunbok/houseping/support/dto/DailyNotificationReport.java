package com.yunbok.houseping.support.dto;

import com.yunbok.houseping.adapter.dto.SubscriptionInfo;

import java.util.List;

/**
 * 일일 알림 리포트 도메인 모델
 *
 * @param receiptEndToday 오늘 마감되는 청약 목록
 * @param receiptStartTomorrow 내일 시작되는 청약 목록
 * @param newSubscriptions 신규 청약 목록
 */
public record DailyNotificationReport(
        List<NotificationTarget> receiptEndToday,
        List<NotificationTarget> receiptStartTomorrow,
        List<SubscriptionInfo> newSubscriptions
) {
    public boolean isEmpty() {
        return receiptEndToday.isEmpty()
                && receiptStartTomorrow.isEmpty()
                && newSubscriptions.isEmpty();
    }

    public int totalCount() {
        return receiptEndToday.size() + receiptStartTomorrow.size() + newSubscriptions.size();
    }
}
