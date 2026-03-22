package com.yunbok.houseping.core.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SubscriptionPrice - 분양가 정보 도메인 모델")
class SubscriptionPriceTest {

    @Nested
    @DisplayName("getTotalSupplyCount() - 총 공급세대수 계산")
    class GetTotalSupplyCount {

        @Test
        @DisplayName("일반공급과 특별공급이 모두 있으면 합산한다")
        void addsBothCounts() {
            // given
            SubscriptionPrice price = SubscriptionPrice.builder()
                    .supplyCount(50)
                    .specialSupplyCount(30)
                    .build();

            // when
            int result = price.getTotalSupplyCount();

            // then
            assertThat(result).isEqualTo(80);
        }

        @Test
        @DisplayName("일반공급이 null이면 특별공급만 반환한다")
        void returnsSpecialOnlyWhenSupplyIsNull() {
            // given
            SubscriptionPrice price = SubscriptionPrice.builder()
                    .supplyCount(null)
                    .specialSupplyCount(30)
                    .build();

            // when
            int result = price.getTotalSupplyCount();

            // then
            assertThat(result).isEqualTo(30);
        }

        @Test
        @DisplayName("특별공급이 null이면 일반공급만 반환한다")
        void returnsGeneralOnlyWhenSpecialIsNull() {
            // given
            SubscriptionPrice price = SubscriptionPrice.builder()
                    .supplyCount(50)
                    .specialSupplyCount(null)
                    .build();

            // when
            int result = price.getTotalSupplyCount();

            // then
            assertThat(result).isEqualTo(50);
        }

        @Test
        @DisplayName("둘 다 null이면 0을 반환한다")
        void returnsZeroWhenBothNull() {
            // given
            SubscriptionPrice price = SubscriptionPrice.builder()
                    .supplyCount(null)
                    .specialSupplyCount(null)
                    .build();

            // when
            int result = price.getTotalSupplyCount();

            // then
            assertThat(result).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("getTopAmountFormatted() - 분양가 포맷")
    class GetTopAmountFormatted {

        @Test
        @DisplayName("분양가를 억/만원 형식으로 포맷한다")
        void formatsAmountInKoreanUnit() {
            // given
            SubscriptionPrice price = SubscriptionPrice.builder()
                    .topAmount(52000L)
                    .build();

            // when
            String result = price.getTopAmountFormatted();

            // then
            assertThat(result).isEqualTo("5억 2,000만");
        }

        @Test
        @DisplayName("분양가가 null이면 '-'를 반환한다")
        void returnsDashWhenNull() {
            // given
            SubscriptionPrice price = SubscriptionPrice.builder()
                    .topAmount(null)
                    .build();

            // when
            String result = price.getTopAmountFormatted();

            // then
            assertThat(result).isEqualTo("-");
        }
    }

    @Nested
    @DisplayName("getPricePerPyeongFormatted() - 평당가 포맷")
    class GetPricePerPyeongFormatted {

        @Test
        @DisplayName("평당가를 만원/평 형식으로 포맷한다")
        void formatsPricePerPyeong() {
            // given
            SubscriptionPrice price = SubscriptionPrice.builder()
                    .pricePerPyeong(2100L)
                    .build();

            // when
            String result = price.getPricePerPyeongFormatted();

            // then
            assertThat(result).isEqualTo("2,100만/평");
        }

        @Test
        @DisplayName("평당가가 null이면 '-'를 반환한다")
        void returnsDashWhenNull() {
            // given
            SubscriptionPrice price = SubscriptionPrice.builder()
                    .pricePerPyeong(null)
                    .build();

            // when
            String result = price.getPricePerPyeongFormatted();

            // then
            assertThat(result).isEqualTo("-");
        }
    }
}
