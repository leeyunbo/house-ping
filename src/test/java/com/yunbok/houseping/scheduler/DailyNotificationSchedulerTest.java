package com.yunbok.houseping.scheduler;

import com.yunbok.houseping.core.service.notification.DailyNotificationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yunbok.houseping.infrastructure.api.SchedulerErrorSlackClient;
import com.yunbok.houseping.infrastructure.formatter.SlackMessageFormatter;

import static org.mockito.Mockito.*;

@DisplayName("DailyNotificationScheduler - 일일 종합 알림 스케줄러")
@ExtendWith(MockitoExtension.class)
class DailyNotificationSchedulerTest {

    @Mock
    private DailyNotificationService dailyNotificationUseCase;

    private DailyNotificationScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new DailyNotificationScheduler(dailyNotificationUseCase, new SchedulerErrorSlackClient("", new SlackMessageFormatter()));
    }

    @Nested
    @DisplayName("sendDailyReport() - 일일 종합 알림 발송 (매일 9시)")
    class SendDailyReport {

        @Test
        @DisplayName("일일 종합 알림을 발송한다")
        void sendsDailyReport() {
            // given
            doNothing().when(dailyNotificationUseCase).sendDailyReport();

            // when
            scheduler.sendDailyReport();

            // then
            verify(dailyNotificationUseCase).sendDailyReport();
        }

        @Test
        @DisplayName("예외가 발생해도 스케줄러는 중단되지 않는다")
        void handlesException() {
            // given
            doThrow(new RuntimeException("Test exception"))
                    .when(dailyNotificationUseCase).sendDailyReport();

            // when
            scheduler.sendDailyReport();

            // then
            verify(dailyNotificationUseCase).sendDailyReport();
        }
    }
}
