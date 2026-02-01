package com.yunbok.houseping.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DailyNotificationReport - 일일 알림 리포트 DTO")
class DailyNotificationReportTest {

    @Nested
    @DisplayName("isEmpty()")
    class IsEmpty {

        @Test
        @DisplayName("모든 리스트가 비어있으면 true를 반환한다")
        void returnsTrueWhenAllEmpty() {
            // given
            DailyNotificationReport report = new DailyNotificationReport(
                    List.of(), List.of(), List.of()
            );

            // then
            assertThat(report.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("receiptEndToday에 데이터가 있으면 false를 반환한다")
        void returnsFalseWhenReceiptEndNotEmpty() {
            // given
            NotificationTarget target = new NotificationTarget(
                    1L, 100L, "테스트", "서울",
                    LocalDate.now(), LocalDate.now(), 100, "https://example.com"
            );
            DailyNotificationReport report = new DailyNotificationReport(
                    List.of(target), List.of(), List.of()
            );

            // then
            assertThat(report.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("receiptStartTomorrow에 데이터가 있으면 false를 반환한다")
        void returnsFalseWhenReceiptStartNotEmpty() {
            // given
            NotificationTarget target = new NotificationTarget(
                    1L, 100L, "테스트", "서울",
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(5), 100, "https://example.com"
            );
            DailyNotificationReport report = new DailyNotificationReport(
                    List.of(), List.of(target), List.of()
            );

            // then
            assertThat(report.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("newSubscriptions에 데이터가 있으면 false를 반환한다")
        void returnsFalseWhenNewSubscriptionsNotEmpty() {
            // given
            SubscriptionInfo subscription = new LhSubscriptionInfo(
                    "테스트", "아파트", "서울",
                    LocalDate.now(), LocalDate.now(), LocalDate.now().plusDays(5),
                    "https://example.com", "접수중"
            );
            DailyNotificationReport report = new DailyNotificationReport(
                    List.of(), List.of(), List.of(subscription)
            );

            // then
            assertThat(report.isEmpty()).isFalse();
        }
    }

    @Nested
    @DisplayName("totalCount()")
    class TotalCount {

        @Test
        @DisplayName("모든 리스트의 총 개수를 반환한다")
        void returnsTotalCount() {
            // given
            NotificationTarget target1 = new NotificationTarget(
                    1L, 100L, "테스트1", "서울",
                    LocalDate.now(), LocalDate.now(), 100, "https://example.com/1"
            );
            NotificationTarget target2 = new NotificationTarget(
                    2L, 200L, "테스트2", "경기",
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(5), 200, "https://example.com/2"
            );
            SubscriptionInfo subscription = new LhSubscriptionInfo(
                    "테스트3", "아파트", "인천",
                    LocalDate.now(), LocalDate.now(), LocalDate.now().plusDays(5),
                    "https://example.com/3", "접수중"
            );

            DailyNotificationReport report = new DailyNotificationReport(
                    List.of(target1),
                    List.of(target2),
                    List.of(subscription)
            );

            // then
            assertThat(report.totalCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("모든 리스트가 비어있으면 0을 반환한다")
        void returnsZeroWhenEmpty() {
            // given
            DailyNotificationReport report = new DailyNotificationReport(
                    List.of(), List.of(), List.of()
            );

            // then
            assertThat(report.totalCount()).isEqualTo(0);
        }
    }
}
