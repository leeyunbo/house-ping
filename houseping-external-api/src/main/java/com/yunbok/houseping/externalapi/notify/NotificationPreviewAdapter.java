package com.yunbok.houseping.externalapi.notify;

import com.yunbok.houseping.core.port.NotificationPreviewPort;
import com.yunbok.houseping.externalapi.formatter.SlackMessageFormatter;
import com.yunbok.houseping.externalapi.formatter.TelegramMessageFormatter;
import com.yunbok.houseping.support.dto.DailyNotificationReport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationPreviewAdapter implements NotificationPreviewPort {

    private final SlackMessageFormatter slackMessageFormatter;
    private final TelegramMessageFormatter telegramMessageFormatter;

    @Override
    public String formatSlackPreview(DailyNotificationReport report) {
        return slackMessageFormatter.formatDailyReport(report);
    }

    @Override
    public String formatTelegramPreview(DailyNotificationReport report) {
        return telegramMessageFormatter.formatDailyReport(report);
    }
}
