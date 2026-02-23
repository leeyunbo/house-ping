package com.yunbok.houseping.scheduler;

import com.yunbok.houseping.core.service.notification.DailyNotificationService;
import com.yunbok.houseping.infrastructure.api.SchedulerErrorSlackClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


/**
 * 일일 종합 알림 스케줄러
 * 신규 청약, 접수 시작, 접수 마감 알림을 하나의 리포트로 통합 발송
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DailyNotificationScheduler {

    private final DailyNotificationService dailyNotificationUseCase;
    private final SchedulerErrorSlackClient errorNotifier;

    /**
     * 매일 오전 9시에 일일 종합 알림 발송
     */
    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
    public void sendDailyReport() {
        log.info("[일일 알림 스케줄러] 실행 시작");
        try {
            dailyNotificationUseCase.sendDailyReport();
            log.info("[일일 알림 스케줄러] 실행 완료");
        } catch (Exception e) {
            log.error("[일일 알림 스케줄러] 실행 실패", e);
            errorNotifier.sendError("일일 알림", e);
        }
    }
}
