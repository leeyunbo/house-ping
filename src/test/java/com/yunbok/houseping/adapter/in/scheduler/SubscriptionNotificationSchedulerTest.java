package com.yunbok.houseping.adapter.in.scheduler;

import com.yunbok.houseping.domain.port.in.SubscriptionNotificationUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@DisplayName("SubscriptionNotificationScheduler - 알림 스케줄러")
@ExtendWith(MockitoExtension.class)
class SubscriptionNotificationSchedulerTest {

    @Mock
    private SubscriptionNotificationUseCase notificationUseCase;

    private SubscriptionNotificationScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new SubscriptionNotificationScheduler(notificationUseCase);
    }

    @Nested
    @DisplayName("sendScheduledNotifications() - 알림 발송")
    class SendScheduledNotifications {

        @Test
        @DisplayName("알림 발송 서비스를 호출한다")
        void callsNotificationService() {
            // given
            when(notificationUseCase.sendScheduledNotifications()).thenReturn(5);

            // when
            scheduler.sendScheduledNotifications();

            // then
            verify(notificationUseCase).sendScheduledNotifications();
        }

        @Test
        @DisplayName("예외가 발생해도 스케줄러가 중단되지 않는다")
        void handlesException() {
            // given
            when(notificationUseCase.sendScheduledNotifications())
                    .thenThrow(new RuntimeException("알림 발송 실패"));

            // when
            scheduler.sendScheduledNotifications();

            // then - 예외 처리됨
            verify(notificationUseCase).sendScheduledNotifications();
        }
    }
}
