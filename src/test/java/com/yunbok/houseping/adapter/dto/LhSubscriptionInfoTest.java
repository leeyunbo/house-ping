package com.yunbok.houseping.adapter.dto;

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
            // given
            LocalDate announceDate = LocalDate.of(2025, 1, 1);
            LocalDate receiptEnd = LocalDate.of(2025, 1, 10);

            // when
            LhSubscriptionInfo info = LhSubscriptionInfo.builder()
                    .houseName("LH 행복주택")
                    .houseType("LH 분양주택")
                    .area("서울")
                    .announceDate(announceDate)
                    .receiptEndDate(receiptEnd)
                    .detailUrl("https://lh.or.kr/detail")
                    .subscriptionStatus("접수중")
                    .build();

            // then
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
    @DisplayName("getDisplayMessage() - 상세 표시 메시지")
    class GetDisplayMessage {

        @Test
        @DisplayName("유형, 지역, 주택명, 공고일, 접수종료일, URL을 포함한 메시지를 반환한다")
        void returnsFormattedMessage() {
            // given
            LhSubscriptionInfo info = LhSubscriptionInfo.builder()
                    .houseName("LH 행복주택 강남")
                    .houseType("LH 분양주택")
                    .area("서울")
                    .announceDate(LocalDate.of(2025, 1, 1))
                    .receiptEndDate(LocalDate.of(2025, 1, 10))
                    .detailUrl("https://lh.or.kr/detail")
                    .build();

            // when
            String message = info.getDisplayMessage();

            // then
            assertThat(message).contains("LH 분양주택");
            assertThat(message).contains("[서울]");
            assertThat(message).contains("LH 행복주택 강남");
            assertThat(message).contains("2025-01-01");
            assertThat(message).contains("2025-01-10");
            assertThat(message).contains("https://lh.or.kr/detail");
        }
    }

    @Nested
    @DisplayName("getSimpleDisplayMessage() - 간략 표시 메시지")
    class GetSimpleDisplayMessage {

        @Test
        @DisplayName("지역과 주택명만 포함한 간략 메시지를 반환한다")
        void returnsSimpleMessage() {
            // given
            LhSubscriptionInfo info = LhSubscriptionInfo.builder()
                    .houseName("LH 행복주택 강남")
                    .area("서울")
                    .build();

            // when
            String message = info.getSimpleDisplayMessage();

            // then
            assertThat(message).isEqualTo("[서울] LH 행복주택 강남\n");
        }
    }

    @Nested
    @DisplayName("equals/hashCode - detailUrl 기준 동등성")
    class EqualsHashCode {

        @Test
        @DisplayName("detailUrl이 같으면 동등한 객체로 판단한다")
        void equalsByDetailUrl() {
            // given
            LhSubscriptionInfo info1 = LhSubscriptionInfo.builder()
                    .houseName("주택A")
                    .area("서울")
                    .detailUrl("https://lh.or.kr/123")
                    .build();

            LhSubscriptionInfo info2 = LhSubscriptionInfo.builder()
                    .houseName("주택B")  // 다른 이름이지만
                    .area("경기")        // 다른 지역이지만
                    .detailUrl("https://lh.or.kr/123")
                    .build();

            // then
            assertThat(info1).isEqualTo(info2);
            assertThat(info1.hashCode()).isEqualTo(info2.hashCode());
        }

        @Test
        @DisplayName("detailUrl이 다르면 다른 객체로 판단한다")
        void notEqualsByDifferentDetailUrl() {
            // given
            LhSubscriptionInfo info1 = LhSubscriptionInfo.builder()
                    .houseName("주택A")
                    .area("서울")
                    .detailUrl("https://lh.or.kr/123")
                    .build();

            LhSubscriptionInfo info2 = LhSubscriptionInfo.builder()
                    .houseName("주택A")  // 같은 이름이지만
                    .area("서울")        // 같은 지역이지만
                    .detailUrl("https://lh.or.kr/456")
                    .build();

            // then
            assertThat(info1).isNotEqualTo(info2);
        }
    }

    @Nested
    @DisplayName("SubscriptionInfo 기본 구현 상속")
    class DefaultImplementation {

        @Test
        @DisplayName("receiptStartDate 기본값은 announceDate를 반환한다")
        void receiptStartDateDefaultsToAnnounceDate() {
            // given
            LocalDate announceDate = LocalDate.of(2025, 1, 1);
            LhSubscriptionInfo info = LhSubscriptionInfo.builder()
                    .houseName("테스트")
                    .area("서울")
                    .announceDate(announceDate)
                    .build();

            // then
            assertThat(info.getReceiptStartDate()).isEqualTo(announceDate);
        }

        @Test
        @DisplayName("winnerAnnounceDate 기본값은 null이다")
        void winnerAnnounceDateDefaultsToNull() {
            // given
            LhSubscriptionInfo info = LhSubscriptionInfo.builder()
                    .houseName("테스트")
                    .area("서울")
                    .build();

            // then
            assertThat(info.getWinnerAnnounceDate()).isNull();
        }

        @Test
        @DisplayName("homepageUrl 기본값은 null이다")
        void homepageUrlDefaultsToNull() {
            // given
            LhSubscriptionInfo info = LhSubscriptionInfo.builder()
                    .houseName("테스트")
                    .area("서울")
                    .build();

            // then
            assertThat(info.getHomepageUrl()).isNull();
        }

        @Test
        @DisplayName("contact 기본값은 null이다")
        void contactDefaultsToNull() {
            // given
            LhSubscriptionInfo info = LhSubscriptionInfo.builder()
                    .houseName("테스트")
                    .area("서울")
                    .build();

            // then
            assertThat(info.getContact()).isNull();
        }

        @Test
        @DisplayName("totalSupplyCount 기본값은 null이다")
        void totalSupplyCountDefaultsToNull() {
            // given
            LhSubscriptionInfo info = LhSubscriptionInfo.builder()
                    .houseName("테스트")
                    .area("서울")
                    .build();

            // then
            assertThat(info.getTotalSupplyCount()).isNull();
        }
    }
}
