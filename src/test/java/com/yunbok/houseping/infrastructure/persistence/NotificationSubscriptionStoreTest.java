package com.yunbok.houseping.infrastructure.persistence;

import com.yunbok.houseping.support.dto.NotificationTarget;
import com.yunbok.houseping.entity.NotificationSubscriptionEntity;
import com.yunbok.houseping.repository.NotificationSubscriptionRepository;
import com.yunbok.houseping.entity.SubscriptionEntity;
import com.yunbok.houseping.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("NotificationSubscriptionStore - 알림 구독 영속성 어댑터")
@ExtendWith(MockitoExtension.class)
class NotificationSubscriptionStoreTest {

    @Mock
    private NotificationSubscriptionRepository notificationSubscriptionRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    private NotificationSubscriptionStore adapter;

    @BeforeEach
    void setUp() {
        adapter = new NotificationSubscriptionStore(
                notificationSubscriptionRepository, subscriptionRepository
        );
    }

    @Nested
    @DisplayName("findPendingReceiptStartTargets() - 접수 시작 알림 대상 조회")
    class FindPendingReceiptStartTargets {

        @Test
        @DisplayName("대기 중인 알림이 없으면 빈 리스트를 반환한다")
        void returnsEmptyWhenNoPending() {
            // given
            when(notificationSubscriptionRepository.findPendingReceiptStartNotifications())
                    .thenReturn(List.of());

            // when
            List<NotificationTarget> result = adapter.findPendingReceiptStartTargets(LocalDate.now());

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("접수 시작일이 일치하는 대상만 반환한다")
        void returnsOnlyMatchingDates() {
            // given
            LocalDate targetDate = LocalDate.of(2025, 1, 15);

            NotificationSubscriptionEntity notification = createNotificationEntity(1L, 100L);
            SubscriptionEntity subscription = createSubscriptionEntity(100L, targetDate, LocalDate.of(2025, 1, 25));

            when(notificationSubscriptionRepository.findPendingReceiptStartNotifications())
                    .thenReturn(List.of(notification));
            when(subscriptionRepository.findAllById(List.of(100L)))
                    .thenReturn(List.of(subscription));

            // when
            List<NotificationTarget> result = adapter.findPendingReceiptStartTargets(targetDate);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).houseName()).isEqualTo("테스트 아파트");
        }

        @Test
        @DisplayName("접수 시작일이 일치하지 않으면 빈 리스트를 반환한다")
        void returnsEmptyWhenDateNotMatching() {
            // given
            LocalDate targetDate = LocalDate.of(2025, 1, 15);
            LocalDate differentDate = LocalDate.of(2025, 1, 20);

            NotificationSubscriptionEntity notification = createNotificationEntity(1L, 100L);
            SubscriptionEntity subscription = createSubscriptionEntity(100L, differentDate, LocalDate.of(2025, 1, 25));

            when(notificationSubscriptionRepository.findPendingReceiptStartNotifications())
                    .thenReturn(List.of(notification));
            when(subscriptionRepository.findAllById(List.of(100L)))
                    .thenReturn(List.of(subscription));

            // when
            List<NotificationTarget> result = adapter.findPendingReceiptStartTargets(targetDate);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("청약 정보가 없으면 필터링된다")
        void filtersOutMissingSubscriptions() {
            // given
            LocalDate targetDate = LocalDate.of(2025, 1, 15);
            NotificationSubscriptionEntity notification = createNotificationEntity(1L, 999L);

            when(notificationSubscriptionRepository.findPendingReceiptStartNotifications())
                    .thenReturn(List.of(notification));
            when(subscriptionRepository.findAllById(List.of(999L)))
                    .thenReturn(List.of());

            // when
            List<NotificationTarget> result = adapter.findPendingReceiptStartTargets(targetDate);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findPendingReceiptEndTargets() - 접수 종료 알림 대상 조회")
    class FindPendingReceiptEndTargets {

        @Test
        @DisplayName("대기 중인 알림이 없으면 빈 리스트를 반환한다")
        void returnsEmptyWhenNoPending() {
            // given
            when(notificationSubscriptionRepository.findPendingReceiptEndNotifications())
                    .thenReturn(List.of());

            // when
            List<NotificationTarget> result = adapter.findPendingReceiptEndTargets(LocalDate.now());

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("접수 종료일이 일치하는 대상만 반환한다")
        void returnsOnlyMatchingDates() {
            // given
            LocalDate targetDate = LocalDate.of(2025, 1, 25);

            NotificationSubscriptionEntity notification = createNotificationEntity(1L, 100L);
            SubscriptionEntity subscription = createSubscriptionEntity(100L, LocalDate.of(2025, 1, 15), targetDate);

            when(notificationSubscriptionRepository.findPendingReceiptEndNotifications())
                    .thenReturn(List.of(notification));
            when(subscriptionRepository.findAllById(List.of(100L)))
                    .thenReturn(List.of(subscription));

            // when
            List<NotificationTarget> result = adapter.findPendingReceiptEndTargets(targetDate);

            // then
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("markReceiptStartNotified() - 접수 시작 알림 완료 처리")
    class MarkReceiptStartNotified {

        @Test
        @DisplayName("알림을 찾으면 접수 시작 알림 완료로 표시한다")
        void marksNotificationWhenFound() {
            // given
            NotificationSubscriptionEntity notification = mock(NotificationSubscriptionEntity.class);
            when(notificationSubscriptionRepository.findById(1L))
                    .thenReturn(Optional.of(notification));

            // when
            adapter.markReceiptStartNotified(1L);

            // then
            verify(notification).markReceiptStartNotified();
        }

        @Test
        @DisplayName("알림이 없으면 아무 것도 하지 않는다")
        void doesNothingWhenNotFound() {
            // given
            when(notificationSubscriptionRepository.findById(999L))
                    .thenReturn(Optional.empty());

            // when
            adapter.markReceiptStartNotified(999L);

            // then - 예외 없이 완료
        }
    }

    @Nested
    @DisplayName("markReceiptEndNotified() - 접수 종료 알림 완료 처리")
    class MarkReceiptEndNotified {

        @Test
        @DisplayName("알림을 찾으면 접수 종료 알림 완료로 표시한다")
        void marksNotificationWhenFound() {
            // given
            NotificationSubscriptionEntity notification = mock(NotificationSubscriptionEntity.class);
            when(notificationSubscriptionRepository.findById(1L))
                    .thenReturn(Optional.of(notification));

            // when
            adapter.markReceiptEndNotified(1L);

            // then
            verify(notification).markReceiptEndNotified();
        }

        @Test
        @DisplayName("알림이 없으면 아무 것도 하지 않는다")
        void doesNothingWhenNotFound() {
            // given
            when(notificationSubscriptionRepository.findById(999L))
                    .thenReturn(Optional.empty());

            // when
            adapter.markReceiptEndNotified(999L);

            // then - 예외 없이 완료
        }
    }

    private NotificationSubscriptionEntity createNotificationEntity(Long id, Long subscriptionId) {
        return NotificationSubscriptionEntity.builder()
                .id(id)
                .subscriptionId(subscriptionId)
                .enabled(true)
                .build();
    }

    private SubscriptionEntity createSubscriptionEntity(Long id, LocalDate receiptStartDate, LocalDate receiptEndDate) {
        return SubscriptionEntity.builder()
                .id(id)
                .source("ApplyHome")
                .houseName("테스트 아파트")
                .area("서울")
                .receiptStartDate(receiptStartDate)
                .receiptEndDate(receiptEndDate)
                .totalSupplyCount(100)
                .detailUrl("https://example.com")
                .collectedAt(LocalDateTime.now())
                .build();
    }
}
