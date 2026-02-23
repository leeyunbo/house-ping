package com.yunbok.houseping.infrastructure.dto;

import com.yunbok.houseping.core.domain.Subscription;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ApplyHomeSubscriptionInfo - 청약Home 청약 정보")
class ApplyHomeSubscriptionInfoTest {

    @Nested
    @DisplayName("빌더를 통한 객체 생성")
    class Builder {

        @Test
        @DisplayName("모든 필드를 설정하여 객체를 생성할 수 있다")
        void canBuildWithAllFields() {
            // given
            LocalDate announceDate = LocalDate.of(2025, 1, 1);
            LocalDate receiptStart = LocalDate.of(2025, 1, 5);
            LocalDate receiptEnd = LocalDate.of(2025, 1, 10);
            LocalDate winnerDate = LocalDate.of(2025, 1, 20);

            // when
            ApplyHomeSubscriptionInfo info = ApplyHomeSubscriptionInfo.builder()
                    .houseManageNo("12345")
                    .pblancNo("PB001")
                    .houseName("테스트아파트")
                    .houseType("APT")
                    .area("서울")
                    .announceDate(announceDate)
                    .receiptStartDate(receiptStart)
                    .receiptEndDate(receiptEnd)
                    .winnerAnnounceDate(winnerDate)
                    .homepageUrl("https://example.com")
                    .detailUrl("https://example.com/detail")
                    .contact("02-1234-5678")
                    .totalSupplyCount(100)
                    .build();

            // then
            assertThat(info.getHouseManageNo()).isEqualTo("12345");
            assertThat(info.getPblancNo()).isEqualTo("PB001");
            assertThat(info.getHouseName()).isEqualTo("테스트아파트");
            assertThat(info.getHouseType()).isEqualTo("APT");
            assertThat(info.getArea()).isEqualTo("서울");
            assertThat(info.getAnnounceDate()).isEqualTo(announceDate);
            assertThat(info.getReceiptStartDate()).isEqualTo(receiptStart);
            assertThat(info.getReceiptEndDate()).isEqualTo(receiptEnd);
            assertThat(info.getWinnerAnnounceDate()).isEqualTo(winnerDate);
            assertThat(info.getHomepageUrl()).isEqualTo("https://example.com");
            assertThat(info.getDetailUrl()).isEqualTo("https://example.com/detail");
            assertThat(info.getContact()).isEqualTo("02-1234-5678");
            assertThat(info.getTotalSupplyCount()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("toSubscription() - 도메인 모델 변환")
    class ToSubscription {

        @Test
        @DisplayName("모든 필드가 도메인 모델로 정확히 변환된다")
        void convertsAllFields() {
            ApplyHomeSubscriptionInfo info = ApplyHomeSubscriptionInfo.builder()
                    .houseManageNo("12345")
                    .pblancNo("PB001")
                    .houseName("테스트아파트")
                    .houseType("APT")
                    .area("서울")
                    .announceDate(LocalDate.of(2025, 1, 1))
                    .receiptStartDate(LocalDate.of(2025, 1, 5))
                    .receiptEndDate(LocalDate.of(2025, 1, 10))
                    .winnerAnnounceDate(LocalDate.of(2025, 1, 20))
                    .homepageUrl("https://example.com")
                    .detailUrl("https://example.com/detail")
                    .contact("02-1234-5678")
                    .totalSupplyCount(100)
                    .address("서울시 강남구")
                    .zipCode("06000")
                    .build();

            Subscription subscription = info.toSubscription();

            assertThat(subscription.getHouseManageNo()).isEqualTo("12345");
            assertThat(subscription.getHouseName()).isEqualTo("테스트아파트");
            assertThat(subscription.getArea()).isEqualTo("서울");
            assertThat(subscription.getTotalSupplyCount()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("equals/hashCode - houseManageNo 기준 동등성")
    class EqualsHashCode {

        @Test
        @DisplayName("houseManageNo가 같으면 동등한 객체로 판단한다")
        void equalsByHouseManageNo() {
            ApplyHomeSubscriptionInfo info1 = ApplyHomeSubscriptionInfo.builder()
                    .houseManageNo("12345")
                    .houseName("아파트A")
                    .build();
            ApplyHomeSubscriptionInfo info2 = ApplyHomeSubscriptionInfo.builder()
                    .houseManageNo("12345")
                    .houseName("아파트B")
                    .build();

            assertThat(info1).isEqualTo(info2);
            assertThat(info1.hashCode()).isEqualTo(info2.hashCode());
        }

        @Test
        @DisplayName("houseManageNo가 다르면 다른 객체로 판단한다")
        void notEqualsByDifferentHouseManageNo() {
            ApplyHomeSubscriptionInfo info1 = ApplyHomeSubscriptionInfo.builder()
                    .houseManageNo("12345")
                    .houseName("아파트A")
                    .build();
            ApplyHomeSubscriptionInfo info2 = ApplyHomeSubscriptionInfo.builder()
                    .houseManageNo("67890")
                    .houseName("아파트A")
                    .build();

            assertThat(info1).isNotEqualTo(info2);
        }
    }

    @Nested
    @DisplayName("Subscription 도메인 로직 (toSubscription() 변환 후)")
    class DomainLogic {

        @Test
        @DisplayName("접수 진행 중인 청약은 isReceiptInProgress()가 true를 반환한다")
        void isReceiptInProgressWhenWithinPeriod() {
            LocalDate today = LocalDate.now();
            Subscription subscription = ApplyHomeSubscriptionInfo.builder()
                    .houseName("테스트아파트").area("서울")
                    .receiptStartDate(today.minusDays(1))
                    .receiptEndDate(today.plusDays(1))
                    .build().toSubscription();

            assertThat(subscription.isReceiptInProgress()).isTrue();
        }

        @Test
        @DisplayName("접수 시작 전인 청약은 isUpcoming()이 true를 반환한다")
        void isUpcomingBeforeStartDate() {
            LocalDate today = LocalDate.now();
            Subscription subscription = ApplyHomeSubscriptionInfo.builder()
                    .houseName("테스트아파트").area("서울")
                    .receiptStartDate(today.plusDays(5))
                    .receiptEndDate(today.plusDays(10))
                    .build().toSubscription();

            assertThat(subscription.isUpcoming()).isTrue();
            assertThat(subscription.isReceiptInProgress()).isFalse();
        }

        @Test
        @DisplayName("접수 마감된 청약은 isExpired()가 true를 반환한다")
        void isExpiredAfterEndDate() {
            LocalDate today = LocalDate.now();
            Subscription subscription = ApplyHomeSubscriptionInfo.builder()
                    .houseName("테스트아파트").area("서울")
                    .receiptStartDate(today.minusDays(10))
                    .receiptEndDate(today.minusDays(1))
                    .build().toSubscription();

            assertThat(subscription.isExpired()).isTrue();
            assertThat(subscription.isReceiptInProgress()).isFalse();
        }

        @Test
        @DisplayName("필수 정보가 있으면 isValid()가 true를 반환한다")
        void isValidWithRequiredFields() {
            Subscription subscription = ApplyHomeSubscriptionInfo.builder()
                    .houseName("테스트아파트").area("서울")
                    .build().toSubscription();

            assertThat(subscription.isValid()).isTrue();
        }

        @Test
        @DisplayName("주택명이 없으면 isValid()가 false를 반환한다")
        void isInvalidWithoutHouseName() {
            Subscription subscription = ApplyHomeSubscriptionInfo.builder()
                    .houseName("").area("서울")
                    .build().toSubscription();

            assertThat(subscription.isValid()).isFalse();
        }

        @Test
        @DisplayName("100세대 이상이면 isLargeSupply()가 true를 반환한다")
        void isLargeSupplyForOver100Units() {
            Subscription subscription = ApplyHomeSubscriptionInfo.builder()
                    .houseName("대단지아파트").area("서울")
                    .totalSupplyCount(100)
                    .build().toSubscription();

            assertThat(subscription.isLargeSupply()).isTrue();
        }

        @Test
        @DisplayName("100세대 미만이면 isLargeSupply()가 false를 반환한다")
        void isNotLargeSupplyForUnder100Units() {
            Subscription subscription = ApplyHomeSubscriptionInfo.builder()
                    .houseName("소단지아파트").area("서울")
                    .totalSupplyCount(50)
                    .build().toSubscription();

            assertThat(subscription.isLargeSupply()).isFalse();
        }
    }
}
