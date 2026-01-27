package com.yunbok.houseping.adapter.in.scheduler;

import com.yunbok.houseping.domain.port.in.SubscriptionNotificationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 청약 알림 스케줄러
 * - 접수 시작 1일 전 알림
 * - 접수 종료 당일 알림
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionNotificationScheduler {

    private final SubscriptionNotificationUseCase notificationUseCase;

    /**
     * 매일 오전 9시에 알림 발송
     */
    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
    public void sendScheduledNotifications() {
        log.info("[알림 스케줄러] 실행 시작");
        try {
            int count = notificationUseCase.sendScheduledNotifications();
            log.info("[알림 스케줄러] 실행 완료 - {}건 발송", count);
        } catch (Exception e) {
            log.error("[알림 스케줄러] 실행 실패", e);
        }
    }
}
