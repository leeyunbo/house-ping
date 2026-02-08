package com.yunbok.houseping.domain.service;

import com.yunbok.houseping.core.service.notification.SubscriptionNotificationService;

import com.yunbok.houseping.support.dto.NotificationTarget;
import com.yunbok.houseping.adapter.persistence.NotificationSubscriptionPersistenceAdapter;
import com.yunbok.houseping.core.port.NotificationSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("SubscriptionNotificationService - 청약 알림 서비스")
@ExtendWith(MockitoExtension.class)
class SubscriptionNotificationServiceTest {

    @Mock
    private NotificationSubscriptionPersistenceAdapter persistencePort;

    @Mock
    private NotificationSender notificationSender;

    private SubscriptionNotificationService service;

    @Nested
    @DisplayName("sendScheduledNotifications() - 예약 알림 발송")
    class SendScheduledNotifications {

        @Test
        @DisplayName("알림 발송기가 없으면 0을 반환한다")
        void returnsZeroWhenNoSender() {
            // given
            service = new SubscriptionNotificationService(persistencePort, Optional.empty());

            // when
            int result = service.sendScheduledNotifications();

            // then
            assertThat(result).isEqualTo(0);
            verify(persistencePort, never()).findPendingReceiptStartTargets(any());
        }

        @Test
        @DisplayName("대상이 없으면 0을 반환한다")
        void returnsZeroWhenNoTargets() {
            // given
            service = new SubscriptionNotificationService(persistencePort, Optional.of(notificationSender));
            when(persistencePort.findPendingReceiptStartTargets(any())).thenReturn(List.of());
            when(persistencePort.findPendingReceiptEndTargets(any())).thenReturn(List.of());

            // when
            int result = service.sendScheduledNotifications();

            // then
            assertThat(result).isEqualTo(0);
        }

        @Test
        @DisplayName("접수 시작 알림을 발송한다")
        void sendsReceiptStartNotifications() {
            // given
            service = new SubscriptionNotificationService(persistencePort, Optional.of(notificationSender));
            NotificationTarget target = createTarget(1L, "테스트 아파트");
            when(persistencePort.findPendingReceiptStartTargets(any())).thenReturn(List.of(target));
            when(persistencePort.findPendingReceiptEndTargets(any())).thenReturn(List.of());

            // when
            int result = service.sendScheduledNotifications();

            // then
            assertThat(result).isEqualTo(1);
            verify(notificationSender).sendNotification(anyString());
            verify(persistencePort).markReceiptStartNotified(1L);
        }

        @Test
        @DisplayName("접수 종료 알림을 발송한다")
        void sendsReceiptEndNotifications() {
            // given
            service = new SubscriptionNotificationService(persistencePort, Optional.of(notificationSender));
            NotificationTarget target = createTarget(1L, "테스트 아파트");
            when(persistencePort.findPendingReceiptStartTargets(any())).thenReturn(List.of());
            when(persistencePort.findPendingReceiptEndTargets(any())).thenReturn(List.of(target));

            // when
            int result = service.sendScheduledNotifications();

            // then
            assertThat(result).isEqualTo(1);
            verify(notificationSender).sendNotification(anyString());
            verify(persistencePort).markReceiptEndNotified(1L);
        }

        @Test
        @DisplayName("여러 대상에게 알림을 발송한다")
        void sendsMultipleNotifications() {
            // given
            service = new SubscriptionNotificationService(persistencePort, Optional.of(notificationSender));
            List<NotificationTarget> startTargets = List.of(
                    createTarget(1L, "아파트1"),
                    createTarget(2L, "아파트2")
            );
            List<NotificationTarget> endTargets = List.of(
                    createTarget(3L, "아파트3")
            );
            when(persistencePort.findPendingReceiptStartTargets(any())).thenReturn(startTargets);
            when(persistencePort.findPendingReceiptEndTargets(any())).thenReturn(endTargets);

            // when
            int result = service.sendScheduledNotifications();

            // then
            assertThat(result).isEqualTo(3);
            verify(notificationSender, times(3)).sendNotification(anyString());
        }

        @Test
        @DisplayName("null 값을 가진 대상도 처리한다")
        void handlesNullValues() {
            // given
            service = new SubscriptionNotificationService(persistencePort, Optional.of(notificationSender));
            NotificationTarget target = new NotificationTarget(
                    1L, 100L, "테스트 아파트", null, null, null, null, null
            );
            when(persistencePort.findPendingReceiptStartTargets(any())).thenReturn(List.of(target));
            when(persistencePort.findPendingReceiptEndTargets(any())).thenReturn(List.of());

            // when
            int result = service.sendScheduledNotifications();

            // then
            assertThat(result).isEqualTo(1);
            verify(notificationSender).sendNotification(anyString());
        }
    }

    private NotificationTarget createTarget(Long id, String houseName) {
        return new NotificationTarget(
                id,
                100L,
                houseName,
                "서울",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(10),
                100,
                "https://example.com"
        );
    }
}
