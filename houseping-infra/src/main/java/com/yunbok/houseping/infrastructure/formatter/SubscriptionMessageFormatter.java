package com.yunbok.houseping.infrastructure.formatter;

import com.yunbok.houseping.core.domain.Subscription;
import com.yunbok.houseping.support.dto.DailyNotificationReport;

import java.util.List;

public interface SubscriptionMessageFormatter {

    String formatBatchSummary(List<Subscription> subscriptions);

    String formatSubscription(Subscription subscription);

    String formatErrorMessage(String errorMessage);

    String formatNoDataMessage();

    /**
     * 일일 종합 알림 리포트를 포맷팅
     */
    String formatDailyReport(DailyNotificationReport report);

    default String formatSupplyCount(Integer count) {
        return count != null ? count + "세대" : "-";
    }
}
