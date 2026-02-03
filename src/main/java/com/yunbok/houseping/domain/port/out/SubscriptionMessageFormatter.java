package com.yunbok.houseping.domain.port.out;

import com.yunbok.houseping.domain.model.DailyNotificationReport;
import com.yunbok.houseping.domain.model.SubscriptionInfo;

import java.util.List;

public interface SubscriptionMessageFormatter {

    String formatBatchSummary(List<SubscriptionInfo> subscriptions);

    String formatSubscription(SubscriptionInfo subscription);

    String formatErrorMessage(String errorMessage);

    String formatNoDataMessage();

    /**
     * 일일 종합 알림 리포트를 포맷팅
     */
    String formatDailyReport(DailyNotificationReport report);
}
