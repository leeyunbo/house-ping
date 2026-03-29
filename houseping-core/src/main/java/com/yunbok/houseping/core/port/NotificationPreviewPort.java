package com.yunbok.houseping.core.port;

import com.yunbok.houseping.support.dto.DailyNotificationReport;

/**
 * 알림 미리보기 포맷 Port
 */
public interface NotificationPreviewPort {

    String formatSlackPreview(DailyNotificationReport report);

    String formatTelegramPreview(DailyNotificationReport report);
}
