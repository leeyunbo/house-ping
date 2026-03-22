package com.yunbok.houseping.scheduler;

import com.yunbok.houseping.core.service.notification.DailyNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@DisplayName("DailyNotificationScheduler - 일일 종합 알림 스케줄러")
@ExtendWith(MockitoExtension.class)
class DailyNotificationSchedulerTest {

    @Mock
    private DailyNotificationService dailyNotificationUseCase;

    private DailyNotificationScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new DailyNotificationScheduler(dailyNotificationUseCase);
    }

    @Nested
    @DisplayName("sendDailyReport() - 일일 종합 알림 발송")
    class SendDailyReport {

        @Test
        @DisplayName("일일 종합 알림을 발송한다")
        void sendsDailyReport() {
            // when
            scheduler.sendDailyReport();

            // then
            verify(dailyNotificationUseCase).sendDailyReport();
        }
    }
}
