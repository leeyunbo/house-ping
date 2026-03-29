package com.yunbok.houseping.scheduler;

import com.yunbok.houseping.core.service.notification.DailyNotificationService;
import com.yunbok.houseping.support.annotation.SchedulerMonitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyNotificationScheduler {

    private final DailyNotificationService dailyNotificationService;

    @SchedulerMonitor("일일 알림")
    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
    public void sendDailyReport() {
        dailyNotificationService.sendDailyReport();
    }
}
