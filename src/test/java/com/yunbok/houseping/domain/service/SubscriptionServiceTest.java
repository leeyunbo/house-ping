package com.yunbok.houseping.domain.service;

import com.yunbok.houseping.domain.model.ApplyHomeSubscriptionInfo;
import com.yunbok.houseping.domain.model.SubscriptionInfo;
import com.yunbok.houseping.domain.port.out.notification.NotificationSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("SubscriptionService - 청약 수집 서비스")
@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionCollector subscriptionCollector;

    @Mock
    private NotificationSender notificationSender;

    private SubscriptionService service;

    @BeforeEach
    void setUp() {
        service = new SubscriptionService(subscriptionCollector, notificationSender);
    }

    @Nested
    @DisplayName("collect() - 청약 수집")
    class Collect {

        @Test
        @DisplayName("notify=true일 때 수집된 청약 정보가 있으면 알림을 발송한다")
        void sendsNotificationWhenSubscriptionsFoundAndNotifyTrue() {
            // given
            LocalDate targetDate = LocalDate.now();
            List<SubscriptionInfo> subscriptions = List.of(
                    createSubscription("힐스테이트 강남"),
                    createSubscription("래미안 판교")
            );
            when(subscriptionCollector.collectFromAllAreas(targetDate)).thenReturn(subscriptions);

            // when
            List<SubscriptionInfo> result = service.collect(targetDate, true);

            // then
            assertThat(result).hasSize(2);
            verify(notificationSender).sendNewSubscriptions(subscriptions);
            verify(notificationSender, never()).sendNotification(anyString());
        }

        @Test
        @DisplayName("notify=true일 때 수집된 청약 정보가 없으면 '없음' 메시지를 발송한다")
        void sendsNoDataMessageWhenEmptyAndNotifyTrue() {
            // given
            LocalDate targetDate = LocalDate.now();
            when(subscriptionCollector.collectFromAllAreas(targetDate)).thenReturn(List.of());

            // when
            List<SubscriptionInfo> result = service.collect(targetDate, true);

            // then
            assertThat(result).isEmpty();
            verify(notificationSender).sendNotification("No new subscriptions for " + targetDate);
            verify(notificationSender, never()).sendNewSubscriptions(any());
        }

        @Test
        @DisplayName("notify=false일 때 알림을 발송하지 않는다")
        void doesNotSendNotificationWhenNotifyFalse() {
            // given
            LocalDate targetDate = LocalDate.of(2025, 6, 15);
            List<SubscriptionInfo> subscriptions = List.of(createSubscription("테스트 아파트"));
            when(subscriptionCollector.collectFromAllAreas(targetDate)).thenReturn(subscriptions);

            // when
            List<SubscriptionInfo> result = service.collect(targetDate, false);

            // then
            assertThat(result).hasSize(1);
            verifyNoInteractions(notificationSender);
        }

        @Test
        @DisplayName("notify=false일 때 빈 결과여도 알림을 발송하지 않는다")
        void doesNotSendNotificationWhenEmptyAndNotifyFalse() {
            // given
            LocalDate targetDate = LocalDate.of(2025, 6, 15);
            when(subscriptionCollector.collectFromAllAreas(targetDate)).thenReturn(List.of());

            // when
            service.collect(targetDate, false);

            // then
            verifyNoInteractions(notificationSender);
        }
    }

    private SubscriptionInfo createSubscription(String houseName) {
        return ApplyHomeSubscriptionInfo.builder()
                .houseName(houseName)
                .area("서울")
                .build();
    }
}
