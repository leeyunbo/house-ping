package com.yunbok.houseping.core.service.notification;

import com.yunbok.houseping.core.port.NotificationSender;
import com.yunbok.houseping.core.port.NotificationSubscriptionPersistencePort;
import com.yunbok.houseping.support.dto.NotificationTarget;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("SubscriptionNotificationService - 청약 알림 서비스")
@ExtendWith(MockitoExtension.class)
class SubscriptionNotificationServiceTest {

    @Mock
    private NotificationSubscriptionPersistencePort persistencePort;

    @Mock
    private NotificationSender notificationSender;

    @Nested
    @DisplayName("sendScheduledNotifications() - 예약 알림 발송")
    class SendScheduledNotifications {

        @Test
        @DisplayName("접수 시작 + 종료 알림을 합산하여 건수를 반환한다")
        void returnsTotalSentCount() {
            // given
            var service = createService(Optional.of(notificationSender));
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            LocalDate today = LocalDate.now();

            when(persistencePort.findPendingReceiptStartTargets(tomorrow))
                    .thenReturn(List.of(createTarget(1L, "아파트A")));
            when(persistencePort.findPendingReceiptEndTargets(today))
                    .thenReturn(List.of(createTarget(2L, "아파트B"), createTarget(3L, "아파트C")));

            // when
            int result = service.sendScheduledNotifications();

            // then
            assertThat(result).isEqualTo(3);
            verify(notificationSender, times(3)).sendNotification(anyString());
        }

        @Test
        @DisplayName("NotificationSender가 없으면 0을 반환하고 아무 것도 안 한다")
        void returnsZeroWhenSenderAbsent() {
            // given
            var service = createService(Optional.empty());

            // when
            int result = service.sendScheduledNotifications();

            // then
            assertThat(result).isEqualTo(0);
            verifyNoInteractions(persistencePort);
        }

        @Test
        @DisplayName("대상이 없으면 0을 반환한다")
        void returnsZeroWhenNoTargets() {
            // given
            var service = createService(Optional.of(notificationSender));
            when(persistencePort.findPendingReceiptStartTargets(any())).thenReturn(List.of());
            when(persistencePort.findPendingReceiptEndTargets(any())).thenReturn(List.of());

            // when
            int result = service.sendScheduledNotifications();

            // then
            assertThat(result).isEqualTo(0);
            verify(notificationSender, never()).sendNotification(anyString());
        }

        @Test
        @DisplayName("접수 시작 알림 메시지에 청약 정보가 포함된다")
        void receiptStartMessageContainsSubscriptionInfo() {
            // given
            var service = createService(Optional.of(notificationSender));
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            NotificationTarget target = new NotificationTarget(
                    1L, 100L, "래미안 원베일리", "서울",
                    LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 5),
                    500, "https://example.com/detail"
            );
            when(persistencePort.findPendingReceiptStartTargets(tomorrow)).thenReturn(List.of(target));
            when(persistencePort.findPendingReceiptEndTargets(any())).thenReturn(List.of());

            // when
            service.sendScheduledNotifications();

            // then
            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            verify(notificationSender).sendNotification(captor.capture());
            String message = captor.getValue();

            assertThat(message).contains("래미안 원베일리");
            assertThat(message).contains("서울");
            assertThat(message).contains("500");
            assertThat(message).contains("내일 접수 시작");
        }

        @Test
        @DisplayName("접수 종료 알림 메시지에 마감 정보가 포함된다")
        void receiptEndMessageContainsDeadlineInfo() {
            // given
            var service = createService(Optional.of(notificationSender));
            LocalDate today = LocalDate.now();
            NotificationTarget target = new NotificationTarget(
                    2L, 200L, "힐스테이트", "경기",
                    LocalDate.of(2026, 3, 15), today,
                    300, null
            );
            when(persistencePort.findPendingReceiptStartTargets(any())).thenReturn(List.of());
            when(persistencePort.findPendingReceiptEndTargets(today)).thenReturn(List.of(target));

            // when
            service.sendScheduledNotifications();

            // then
            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            verify(notificationSender).sendNotification(captor.capture());
            String message = captor.getValue();

            assertThat(message).contains("힐스테이트");
            assertThat(message).contains("오늘 접수 마감");
        }

        @Test
        @DisplayName("발송 후 알림 상태를 notified로 마킹한다")
        void marksNotifiedAfterSending() {
            // given
            var service = createService(Optional.of(notificationSender));
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            when(persistencePort.findPendingReceiptStartTargets(tomorrow))
                    .thenReturn(List.of(createTarget(10L, "아파트A"), createTarget(20L, "아파트B")));
            when(persistencePort.findPendingReceiptEndTargets(any())).thenReturn(List.of());

            // when
            service.sendScheduledNotifications();

            // then
            verify(persistencePort).markReceiptStartNotified(10L);
            verify(persistencePort).markReceiptStartNotified(20L);
        }

        @Test
        @DisplayName("area가 null이면 '-'로 대체한다")
        void replacesNullAreaWithDash() {
            // given
            var service = createService(Optional.of(notificationSender));
            NotificationTarget target = new NotificationTarget(
                    1L, 100L, "테스트아파트", null,
                    LocalDate.of(2026, 4, 1), null, null, null
            );
            when(persistencePort.findPendingReceiptStartTargets(any())).thenReturn(List.of(target));
            when(persistencePort.findPendingReceiptEndTargets(any())).thenReturn(List.of());

            // when
            service.sendScheduledNotifications();

            // then
            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            verify(notificationSender).sendNotification(captor.capture());
            assertThat(captor.getValue()).contains("지역: -");
        }
    }

    private SubscriptionNotificationService createService(Optional<NotificationSender> sender) {
        return new SubscriptionNotificationService(persistencePort, sender);
    }

    private NotificationTarget createTarget(Long notificationId, String houseName) {
        return new NotificationTarget(
                notificationId, notificationId * 10, houseName, "서울",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5),
                100, "https://example.com"
        );
    }
}
