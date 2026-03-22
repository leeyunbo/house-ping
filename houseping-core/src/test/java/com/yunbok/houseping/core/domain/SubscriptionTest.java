package com.yunbok.houseping.core.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Subscription - 청약 정보 도메인 모델")
class SubscriptionTest {

    @Nested
    @DisplayName("isReceiptInProgress() - 접수 진행중 여부 확인")
    class IsReceiptInProgress {

        @Test
        @DisplayName("오늘이 접수 시작일과 종료일 사이이면 true를 반환한다")
        void returnsTrueWhenTodayIsBetweenStartAndEnd() {
            // given
            Subscription subscription = Subscription.builder()
                    .receiptStartDate(LocalDate.of(2026, 3, 20))
                    .receiptEndDate(LocalDate.of(2026, 3, 25))
                    .build();

            // when
            boolean result = subscription.isReceiptInProgress(LocalDate.of(2026, 3, 22));

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("오늘이 접수 시작일과 같으면 true를 반환한다")
        void returnsTrueWhenTodayEqualsStart() {
            // given
            Subscription subscription = Subscription.builder()
                    .receiptStartDate(LocalDate.of(2026, 3, 22))
                    .receiptEndDate(LocalDate.of(2026, 3, 25))
                    .build();

            // when
            boolean result = subscription.isReceiptInProgress(LocalDate.of(2026, 3, 22));

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("오늘이 접수 종료일과 같으면 true를 반환한다")
        void returnsTrueWhenTodayEqualsEnd() {
            // given
            Subscription subscription = Subscription.builder()
                    .receiptStartDate(LocalDate.of(2026, 3, 20))
                    .receiptEndDate(LocalDate.of(2026, 3, 22))
                    .build();

            // when
            boolean result = subscription.isReceiptInProgress(LocalDate.of(2026, 3, 22));

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("오늘이 접수 시작일 이전이면 false를 반환한다")
        void returnsFalseWhenTodayBeforeStart() {
            // given
            Subscription subscription = Subscription.builder()
                    .receiptStartDate(LocalDate.of(2026, 3, 25))
                    .receiptEndDate(LocalDate.of(2026, 3, 30))
                    .build();

            // when
            boolean result = subscription.isReceiptInProgress(LocalDate.of(2026, 3, 22));

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("오늘이 접수 종료일 이후이면 false를 반환한다")
        void returnsFalseWhenTodayAfterEnd() {
            // given
            Subscription subscription = Subscription.builder()
                    .receiptStartDate(LocalDate.of(2026, 3, 10))
                    .receiptEndDate(LocalDate.of(2026, 3, 15))
                    .build();

            // when
            boolean result = subscription.isReceiptInProgress(LocalDate.of(2026, 3, 22));

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("종료일이 null이고 오늘이 시작일 이후이면 true를 반환한다")
        void returnsTrueWhenEndIsNullAndTodayAfterStart() {
            // given
            Subscription subscription = Subscription.builder()
                    .receiptStartDate(LocalDate.of(2026, 3, 20))
                    .receiptEndDate(null)
                    .build();

            // when
            boolean result = subscription.isReceiptInProgress(LocalDate.of(2026, 3, 22));

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("시작일이 null이면 false를 반환한다")
        void returnsFalseWhenStartIsNull() {
            // given
            Subscription subscription = Subscription.builder()
                    .receiptStartDate(null)
                    .receiptEndDate(LocalDate.of(2026, 3, 25))
                    .build();

            // when
            boolean result = subscription.isReceiptInProgress(LocalDate.of(2026, 3, 22));

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("isUpcoming() - 접수 예정 여부 확인")
    class IsUpcoming {

        @Test
        @DisplayName("오늘이 접수 시작일 이전이면 true를 반환한다")
        void returnsTrueWhenTodayBeforeStart() {
            // given
            Subscription subscription = Subscription.builder()
                    .receiptStartDate(LocalDate.of(2026, 3, 25))
                    .build();

            // when
            boolean result = subscription.isUpcoming(LocalDate.of(2026, 3, 22));

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("오늘이 접수 시작일과 같으면 false를 반환한다")
        void returnsFalseWhenTodayEqualsStart() {
            // given
            Subscription subscription = Subscription.builder()
                    .receiptStartDate(LocalDate.of(2026, 3, 22))
                    .build();

            // when
            boolean result = subscription.isUpcoming(LocalDate.of(2026, 3, 22));

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("시작일이 null이면 false를 반환한다")
        void returnsFalseWhenStartIsNull() {
            // given
            Subscription subscription = Subscription.builder()
                    .receiptStartDate(null)
                    .build();

            // when
            boolean result = subscription.isUpcoming(LocalDate.of(2026, 3, 22));

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("getStatus() - 청약 상태 반환")
    class GetStatus {

        @Test
        @DisplayName("접수 진행중이면 ACTIVE를 반환한다")
        void returnsActiveWhenInProgress() {
            // given
            Subscription subscription = Subscription.builder()
                    .receiptStartDate(LocalDate.of(2026, 3, 20))
                    .receiptEndDate(LocalDate.of(2026, 3, 25))
                    .build();

            // when
            SubscriptionStatus status = subscription.getStatus(LocalDate.of(2026, 3, 22));

            // then
            assertThat(status).isEqualTo(SubscriptionStatus.ACTIVE);
        }

        @Test
        @DisplayName("접수 예정이면 UPCOMING을 반환한다")
        void returnsUpcomingWhenBeforeStart() {
            // given
            Subscription subscription = Subscription.builder()
                    .receiptStartDate(LocalDate.of(2026, 3, 25))
                    .receiptEndDate(LocalDate.of(2026, 3, 30))
                    .build();

            // when
            SubscriptionStatus status = subscription.getStatus(LocalDate.of(2026, 3, 22));

            // then
            assertThat(status).isEqualTo(SubscriptionStatus.UPCOMING);
        }

        @Test
        @DisplayName("접수 마감이면 CLOSED를 반환한다")
        void returnsClosedWhenExpired() {
            // given
            Subscription subscription = Subscription.builder()
                    .receiptStartDate(LocalDate.of(2026, 3, 10))
                    .receiptEndDate(LocalDate.of(2026, 3, 15))
                    .build();

            // when
            SubscriptionStatus status = subscription.getStatus(LocalDate.of(2026, 3, 22));

            // then
            assertThat(status).isEqualTo(SubscriptionStatus.CLOSED);
        }
    }

    @Nested
    @DisplayName("isValid() - 유효성 확인")
    class IsValid {

        @Test
        @DisplayName("이름과 지역이 모두 있으면 true를 반환한다")
        void returnsTrueWhenNameAndAreaPresent() {
            // given
            Subscription subscription = Subscription.builder()
                    .houseName("래미안 원베일리")
                    .area("서울")
                    .build();

            // when & then
            assertThat(subscription.isValid()).isTrue();
        }

        @Test
        @DisplayName("이름이 null이면 false를 반환한다")
        void returnsFalseWhenNameIsNull() {
            // given
            Subscription subscription = Subscription.builder()
                    .houseName(null)
                    .area("서울")
                    .build();

            // when & then
            assertThat(subscription.isValid()).isFalse();
        }

        @Test
        @DisplayName("지역이 빈 문자열이면 false를 반환한다")
        void returnsFalseWhenAreaIsBlank() {
            // given
            Subscription subscription = Subscription.builder()
                    .houseName("래미안 원베일리")
                    .area("  ")
                    .build();

            // when & then
            assertThat(subscription.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("isLargeSupply() - 대단지 여부 확인")
    class IsLargeSupply {

        @Test
        @DisplayName("공급세대수가 100이면 true를 반환한다")
        void returnsTrueWhenCountIs100() {
            // given
            Subscription subscription = Subscription.builder()
                    .totalSupplyCount(100)
                    .build();

            // when & then
            assertThat(subscription.isLargeSupply()).isTrue();
        }

        @Test
        @DisplayName("공급세대수가 99이면 false를 반환한다")
        void returnsFalseWhenCountIs99() {
            // given
            Subscription subscription = Subscription.builder()
                    .totalSupplyCount(99)
                    .build();

            // when & then
            assertThat(subscription.isLargeSupply()).isFalse();
        }

        @Test
        @DisplayName("공급세대수가 null이면 false를 반환한다")
        void returnsFalseWhenCountIsNull() {
            // given
            Subscription subscription = Subscription.builder()
                    .totalSupplyCount(null)
                    .build();

            // when & then
            assertThat(subscription.isLargeSupply()).isFalse();
        }
    }

    @Nested
    @DisplayName("getDisplayMessage() - 표시 메시지 생성")
    class GetDisplayMessage {

        @Test
        @DisplayName("모든 필드가 있으면 전체 메시지를 생성한다")
        void generatesFullMessageWithAllFields() {
            // given
            Subscription subscription = Subscription.builder()
                    .area("서울")
                    .houseName("래미안 원베일리")
                    .totalSupplyCount(500)
                    .receiptStartDate(LocalDate.of(2026, 3, 20))
                    .receiptEndDate(LocalDate.of(2026, 3, 25))
                    .detailUrl("https://example.com/detail")
                    .build();

            // when
            String message = subscription.getDisplayMessage();

            // then
            assertThat(message).isEqualTo(
                    "[서울] 래미안 원베일리 (500세대)\n접수: 2026-03-20 ~ 2026-03-25\nhttps://example.com/detail");
        }

        @Test
        @DisplayName("선택 필드가 없으면 해당 부분을 생략한다")
        void omitsOptionalFields() {
            // given
            Subscription subscription = Subscription.builder()
                    .area("경기")
                    .houseName("힐스테이트")
                    .totalSupplyCount(null)
                    .receiptStartDate(LocalDate.of(2026, 3, 20))
                    .receiptEndDate(null)
                    .detailUrl(null)
                    .build();

            // when
            String message = subscription.getDisplayMessage();

            // then
            assertThat(message).isEqualTo("[경기] 힐스테이트\n접수: 2026-03-20");
        }
    }
}
