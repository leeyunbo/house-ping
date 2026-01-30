package com.yunbok.houseping.infrastructure.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NotificationSubscriptionEntity - 알림 구독 엔티티")
class NotificationSubscriptionEntityTest {

    @Test
    @DisplayName("기본 빌더로 생성 시 enabled는 true이다")
    void defaultEnabledIsTrue() {
        // when
        NotificationSubscriptionEntity entity = NotificationSubscriptionEntity.builder()
                .subscriptionId(1L)
                .build();

        // then
        assertThat(entity.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("기본 빌더로 생성 시 receiptStartNotified는 false이다")
    void defaultReceiptStartNotifiedIsFalse() {
        // when
        NotificationSubscriptionEntity entity = NotificationSubscriptionEntity.builder()
                .subscriptionId(1L)
                .build();

        // then
        assertThat(entity.isReceiptStartNotified()).isFalse();
    }

    @Test
    @DisplayName("기본 빌더로 생성 시 receiptEndNotified는 false이다")
    void defaultReceiptEndNotifiedIsFalse() {
        // when
        NotificationSubscriptionEntity entity = NotificationSubscriptionEntity.builder()
                .subscriptionId(1L)
                .build();

        // then
        assertThat(entity.isReceiptEndNotified()).isFalse();
    }

    @Nested
    @DisplayName("markReceiptStartNotified() - 접수 시작 알림 완료 표시")
    class MarkReceiptStartNotified {

        @Test
        @DisplayName("receiptStartNotified를 true로 변경한다")
        void setsReceiptStartNotifiedToTrue() {
            // given
            NotificationSubscriptionEntity entity = NotificationSubscriptionEntity.builder()
                    .subscriptionId(1L)
                    .receiptStartNotified(false)
                    .build();

            // when
            entity.markReceiptStartNotified();

            // then
            assertThat(entity.isReceiptStartNotified()).isTrue();
        }
    }

    @Nested
    @DisplayName("markReceiptEndNotified() - 접수 종료 알림 완료 표시")
    class MarkReceiptEndNotified {

        @Test
        @DisplayName("receiptEndNotified를 true로 변경한다")
        void setsReceiptEndNotifiedToTrue() {
            // given
            NotificationSubscriptionEntity entity = NotificationSubscriptionEntity.builder()
                    .subscriptionId(1L)
                    .receiptEndNotified(false)
                    .build();

            // when
            entity.markReceiptEndNotified();

            // then
            assertThat(entity.isReceiptEndNotified()).isTrue();
        }
    }

    @Nested
    @DisplayName("toggleEnabled() - 활성화 상태 토글")
    class ToggleEnabled {

        @Test
        @DisplayName("true에서 false로 변경한다")
        void togglesFromTrueToFalse() {
            // given
            NotificationSubscriptionEntity entity = NotificationSubscriptionEntity.builder()
                    .subscriptionId(1L)
                    .enabled(true)
                    .build();

            // when
            entity.toggleEnabled();

            // then
            assertThat(entity.isEnabled()).isFalse();
        }

        @Test
        @DisplayName("false에서 true로 변경한다")
        void togglesFromFalseToTrue() {
            // given
            NotificationSubscriptionEntity entity = NotificationSubscriptionEntity.builder()
                    .subscriptionId(1L)
                    .enabled(false)
                    .build();

            // when
            entity.toggleEnabled();

            // then
            assertThat(entity.isEnabled()).isTrue();
        }
    }

    @Nested
    @DisplayName("disable() - 비활성화")
    class Disable {

        @Test
        @DisplayName("enabled를 false로 변경한다")
        void setsEnabledToFalse() {
            // given
            NotificationSubscriptionEntity entity = NotificationSubscriptionEntity.builder()
                    .subscriptionId(1L)
                    .enabled(true)
                    .build();

            // when
            entity.disable();

            // then
            assertThat(entity.isEnabled()).isFalse();
        }

        @Test
        @DisplayName("이미 비활성화 상태여도 false를 유지한다")
        void keepsFalseWhenAlreadyDisabled() {
            // given
            NotificationSubscriptionEntity entity = NotificationSubscriptionEntity.builder()
                    .subscriptionId(1L)
                    .enabled(false)
                    .build();

            // when
            entity.disable();

            // then
            assertThat(entity.isEnabled()).isFalse();
        }
    }

    @Test
    @DisplayName("모든 필드를 설정하여 생성할 수 있다")
    void canCreateWithAllFields() {
        // when
        NotificationSubscriptionEntity entity = NotificationSubscriptionEntity.builder()
                .id(1L)
                .subscriptionId(100L)
                .enabled(false)
                .receiptStartNotified(true)
                .receiptEndNotified(true)
                .build();

        // then
        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getSubscriptionId()).isEqualTo(100L);
        assertThat(entity.isEnabled()).isFalse();
        assertThat(entity.isReceiptStartNotified()).isTrue();
        assertThat(entity.isReceiptEndNotified()).isTrue();
    }
}
