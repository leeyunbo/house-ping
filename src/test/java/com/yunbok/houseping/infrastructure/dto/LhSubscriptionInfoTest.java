package com.yunbok.houseping.infrastructure.dto;

import com.yunbok.houseping.core.domain.Subscription;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LhSubscriptionInfo - LH 청약 정보")
class LhSubscriptionInfoTest {

    @Nested
    @DisplayName("빌더를 통한 객체 생성")
    class Builder {

        @Test
        @DisplayName("모든 필드를 설정하여 객체를 생성할 수 있다")
        void canBuildWithAllFields() {
            LocalDate announceDate = LocalDate.of(2025, 1, 1);
            LocalDate receiptEnd = LocalDate.of(2025, 1, 10);

            LhSubscriptionInfo info = LhSubscriptionInfo.builder()
                    .houseName("LH 행복주택")
                    .houseType("LH 분양주택")
                    .area("서울")
                    .announceDate(announceDate)
                    .receiptEndDate(receiptEnd)
                    .detailUrl("https://lh.or.kr/detail")
                    .subscriptionStatus("접수중")
                    .build();

            assertThat(info.getHouseName()).isEqualTo("LH 행복주택");
            assertThat(info.getHouseType()).isEqualTo("LH 분양주택");
            assertThat(info.getArea()).isEqualTo("서울");
            assertThat(info.getAnnounceDate()).isEqualTo(announceDate);
            assertThat(info.getReceiptEndDate()).isEqualTo(receiptEnd);
            assertThat(info.getDetailUrl()).isEqualTo("https://lh.or.kr/detail");
            assertThat(info.getSubscriptionStatus()).isEqualTo("접수중");
        }
    }

    @Nested
    @DisplayName("toSubscription() - 도메인 모델 변환")
    class ToSubscription {

        @Test
        @DisplayName("모든 필드가 도메인 모델로 정확히 변환된다")
        void convertsAllFields() {
            LhSubscriptionInfo info = LhSubscriptionInfo.builder()
                    .houseName("LH 행복주택")
                    .houseType("LH 분양주택")
                    .area("서울")
                    .announceDate(LocalDate.of(2025, 1, 1))
                    .receiptStartDate(LocalDate.of(2025, 1, 5))
                    .receiptEndDate(LocalDate.of(2025, 1, 10))
                    .detailUrl("https://lh.or.kr/detail")
                    .build();

            Subscription subscription = info.toSubscription();

            assertThat(subscription.getHouseName()).isEqualTo("LH 행복주택");
            assertThat(subscription.getArea()).isEqualTo("서울");
            assertThat(subscription.getReceiptStartDate()).isEqualTo(LocalDate.of(2025, 1, 5));
        }

        @Test
        @DisplayName("receiptStartDate가 없으면 announceDate로 변환된다")
        void usesAnnounceDateAsDefault() {
            LocalDate announceDate = LocalDate.of(2025, 1, 1);
            LhSubscriptionInfo info = LhSubscriptionInfo.builder()
                    .houseName("테스트").area("서울")
                    .announceDate(announceDate)
                    .build();

            Subscription subscription = info.toSubscription();

            assertThat(subscription.getReceiptStartDate()).isEqualTo(announceDate);
        }
    }

    @Nested
    @DisplayName("equals/hashCode - detailUrl 기준 동등성")
    class EqualsHashCode {

        @Test
        @DisplayName("detailUrl이 같으면 동등한 객체로 판단한다")
        void equalsByDetailUrl() {
            LhSubscriptionInfo info1 = LhSubscriptionInfo.builder()
                    .houseName("주택A").area("서울")
                    .detailUrl("https://lh.or.kr/123")
                    .build();
            LhSubscriptionInfo info2 = LhSubscriptionInfo.builder()
                    .houseName("주택B").area("경기")
                    .detailUrl("https://lh.or.kr/123")
                    .build();

            assertThat(info1).isEqualTo(info2);
            assertThat(info1.hashCode()).isEqualTo(info2.hashCode());
        }

        @Test
        @DisplayName("detailUrl이 다르면 다른 객체로 판단한다")
        void notEqualsByDifferentDetailUrl() {
            LhSubscriptionInfo info1 = LhSubscriptionInfo.builder()
                    .houseName("주택A").area("서울")
                    .detailUrl("https://lh.or.kr/123")
                    .build();
            LhSubscriptionInfo info2 = LhSubscriptionInfo.builder()
                    .houseName("주택A").area("서울")
                    .detailUrl("https://lh.or.kr/456")
                    .build();

            assertThat(info1).isNotEqualTo(info2);
        }
    }

    @Nested
    @DisplayName("getReceiptStartDate() - receiptStartDate 기본값")
    class ReceiptStartDate {

        @Test
        @DisplayName("receiptStartDate 기본값은 announceDate를 반환한다")
        void receiptStartDateDefaultsToAnnounceDate() {
            LocalDate announceDate = LocalDate.of(2025, 1, 1);
            LhSubscriptionInfo info = LhSubscriptionInfo.builder()
                    .houseName("테스트").area("서울")
                    .announceDate(announceDate)
                    .build();

            assertThat(info.getReceiptStartDate()).isEqualTo(announceDate);
        }
    }
}
