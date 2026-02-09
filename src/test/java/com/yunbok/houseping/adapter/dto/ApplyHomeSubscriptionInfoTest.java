package com.yunbok.houseping.adapter.dto;

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
    @DisplayName("getDisplayMessage() - 상세 표시 메시지")
    class GetDisplayMessage {

        @Test
        @DisplayName("지역, 주택명, 접수기간, 발표일, 세대수, URL을 포함한 메시지를 반환한다")
        void returnsFormattedMessage() {
            // given
            ApplyHomeSubscriptionInfo info = ApplyHomeSubscriptionInfo.builder()
                    .houseName("힐스테이트 강남")
                    .area("서울")
                    .receiptStartDate(LocalDate.of(2025, 1, 5))
                    .receiptEndDate(LocalDate.of(2025, 1, 10))
                    .winnerAnnounceDate(LocalDate.of(2025, 1, 20))
                    .totalSupplyCount(500)
                    .detailUrl("https://apply.com/detail")
                    .build();

            // when
            String message = info.getDisplayMessage();

            // then
            assertThat(message).contains("[서울]");
            assertThat(message).contains("힐스테이트 강남");
            assertThat(message).contains("2025-01-05");
            assertThat(message).contains("2025-01-10");
            assertThat(message).contains("2025-01-20");
            assertThat(message).contains("500세대");
            assertThat(message).contains("https://apply.com/detail");
        }
    }

    @Nested
    @DisplayName("getSimpleDisplayMessage() - 간략 표시 메시지")
    class GetSimpleDisplayMessage {

        @Test
        @DisplayName("지역과 주택명만 포함한 간략 메시지를 반환한다")
        void returnsSimpleMessage() {
            // given
            ApplyHomeSubscriptionInfo info = ApplyHomeSubscriptionInfo.builder()
                    .houseName("힐스테이트 강남")
                    .area("서울")
                    .build();

            // when
            String message = info.getSimpleDisplayMessage();

            // then
            assertThat(message).isEqualTo("[서울] 힐스테이트 강남\n");
        }
    }

    @Nested
    @DisplayName("equals/hashCode - houseManageNo 기준 동등성")
    class EqualsHashCode {

        @Test
        @DisplayName("houseManageNo가 같으면 동등한 객체로 판단한다")
        void equalsByHouseManageNo() {
            // given
            ApplyHomeSubscriptionInfo info1 = ApplyHomeSubscriptionInfo.builder()
                    .houseManageNo("12345")
                    .houseName("아파트A")
                    .build();

            ApplyHomeSubscriptionInfo info2 = ApplyHomeSubscriptionInfo.builder()
                    .houseManageNo("12345")
                    .houseName("아파트B")  // 다른 이름이지만
                    .build();

            // then
            assertThat(info1).isEqualTo(info2);
            assertThat(info1.hashCode()).isEqualTo(info2.hashCode());
        }

        @Test
        @DisplayName("houseManageNo가 다르면 다른 객체로 판단한다")
        void notEqualsByDifferentHouseManageNo() {
            // given
            ApplyHomeSubscriptionInfo info1 = ApplyHomeSubscriptionInfo.builder()
                    .houseManageNo("12345")
                    .houseName("아파트A")
                    .build();

            ApplyHomeSubscriptionInfo info2 = ApplyHomeSubscriptionInfo.builder()
                    .houseManageNo("67890")
                    .houseName("아파트A")  // 같은 이름이지만
                    .build();

            // then
            assertThat(info1).isNotEqualTo(info2);
        }
    }

    @Nested
    @DisplayName("SubscriptionInfo 도메인 로직")
    class DomainLogic {

        @Test
        @DisplayName("접수 진행 중인 청약은 isReceiptInProgress()가 true를 반환한다")
        void isReceiptInProgressWhenWithinPeriod() {
            // given
            LocalDate today = LocalDate.now();
            ApplyHomeSubscriptionInfo info = ApplyHomeSubscriptionInfo.builder()
                    .houseName("테스트아파트")
                    .area("서울")
                    .receiptStartDate(today.minusDays(1))
                    .receiptEndDate(today.plusDays(1))
                    .build();

            // then
            assertThat(info.isReceiptInProgress()).isTrue();
        }

        @Test
        @DisplayName("접수 시작 전인 청약은 isUpcoming()이 true를 반환한다")
        void isUpcomingBeforeStartDate() {
            // given
            LocalDate today = LocalDate.now();
            ApplyHomeSubscriptionInfo info = ApplyHomeSubscriptionInfo.builder()
                    .houseName("테스트아파트")
                    .area("서울")
                    .receiptStartDate(today.plusDays(5))
                    .receiptEndDate(today.plusDays(10))
                    .build();

            // then
            assertThat(info.isUpcoming()).isTrue();
            assertThat(info.isReceiptInProgress()).isFalse();
        }

        @Test
        @DisplayName("접수 마감된 청약은 isExpired()가 true를 반환한다")
        void isExpiredAfterEndDate() {
            // given
            LocalDate today = LocalDate.now();
            ApplyHomeSubscriptionInfo info = ApplyHomeSubscriptionInfo.builder()
                    .houseName("테스트아파트")
                    .area("서울")
                    .receiptStartDate(today.minusDays(10))
                    .receiptEndDate(today.minusDays(1))
                    .build();

            // then
            assertThat(info.isExpired()).isTrue();
            assertThat(info.isReceiptInProgress()).isFalse();
        }

        @Test
        @DisplayName("필수 정보가 있으면 isValid()가 true를 반환한다")
        void isValidWithRequiredFields() {
            // given
            ApplyHomeSubscriptionInfo info = ApplyHomeSubscriptionInfo.builder()
                    .houseName("테스트아파트")
                    .area("서울")
                    .build();

            // then
            assertThat(info.isValid()).isTrue();
        }

        @Test
        @DisplayName("주택명이 없으면 isValid()가 false를 반환한다")
        void isInvalidWithoutHouseName() {
            // given
            ApplyHomeSubscriptionInfo info = ApplyHomeSubscriptionInfo.builder()
                    .houseName("")
                    .area("서울")
                    .build();

            // then
            assertThat(info.isValid()).isFalse();
        }

        @Test
        @DisplayName("100세대 이상이면 isLargeSupply()가 true를 반환한다")
        void isLargeSupplyForOver100Units() {
            // given
            ApplyHomeSubscriptionInfo info = ApplyHomeSubscriptionInfo.builder()
                    .houseName("대단지아파트")
                    .area("서울")
                    .totalSupplyCount(100)
                    .build();

            // then
            assertThat(info.isLargeSupply()).isTrue();
        }

        @Test
        @DisplayName("100세대 미만이면 isLargeSupply()가 false를 반환한다")
        void isNotLargeSupplyForUnder100Units() {
            // given
            ApplyHomeSubscriptionInfo info = ApplyHomeSubscriptionInfo.builder()
                    .houseName("소단지아파트")
                    .area("서울")
                    .totalSupplyCount(50)
                    .build();

            // then
            assertThat(info.isLargeSupply()).isFalse();
        }
    }

    @Nested
    @DisplayName("Optional 반환 메서드")
    class OptionalMethods {

        @Test
        @DisplayName("getWinnerAnnounceDateOpt()은 발표일이 있으면 Optional에 담아 반환한다")
        void returnsOptionalWithValue() {
            // given
            LocalDate winnerDate = LocalDate.of(2025, 1, 20);
            ApplyHomeSubscriptionInfo info = ApplyHomeSubscriptionInfo.builder()
                    .houseName("테스트")
                    .area("서울")
                    .winnerAnnounceDate(winnerDate)
                    .build();

            // then
            assertThat(info.getWinnerAnnounceDateOpt()).isPresent();
            assertThat(info.getWinnerAnnounceDateOpt()).contains(winnerDate);
        }

        @Test
        @DisplayName("getWinnerAnnounceDateOpt()은 발표일이 없으면 빈 Optional을 반환한다")
        void returnsEmptyOptionalWhenNull() {
            // given
            ApplyHomeSubscriptionInfo info = ApplyHomeSubscriptionInfo.builder()
                    .houseName("테스트")
                    .area("서울")
                    .winnerAnnounceDate(null)
                    .build();

            // then
            assertThat(info.getWinnerAnnounceDateOpt()).isEmpty();
        }
    }
}
