package com.yunbok.houseping.core.port;

import com.yunbok.houseping.support.dto.DailyNotificationReport;

/**
 * 일일 종합 리포트 알림 아웃바운드 포트
 */
public interface DailyReportNotifier {

    /**
     * 일일 종합 알림 리포트 발송
     */
    void sendDailyReport(DailyNotificationReport report);
}
