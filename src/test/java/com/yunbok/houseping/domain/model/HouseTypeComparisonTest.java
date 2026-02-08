package com.yunbok.houseping.domain.model;

import com.yunbok.houseping.support.dto.HouseTypeComparison;
import com.yunbok.houseping.core.domain.RealTransaction;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HouseTypeComparison - 주택형별 시세 비교")
class HouseTypeComparisonTest {

    @Nested
    @DisplayName("getEstimatedProfitFormatted() - 예상 차익 포맷")
    class GetEstimatedProfitFormatted {

        @Test
        @DisplayName("양수 차익은 '+' 부호를 붙인다")
        void addsPositiveSignForProfit() {
            // given
            HouseTypeComparison comparison = HouseTypeComparison.builder()
                    .houseType("84A")
                    .estimatedProfit(17000L)
                    .build();

            // when
            String formatted = comparison.getEstimatedProfitFormatted();

            // then
            assertThat(formatted).isEqualTo("+1억 7,000만");
        }

        @Test
        @DisplayName("음수 차익은 '-' 부호를 붙인다")
        void addsNegativeSignForLoss() {
            // given
            HouseTypeComparison comparison = HouseTypeComparison.builder()
                    .houseType("84A")
                    .estimatedProfit(-5000L)
                    .build();

            // when
            String formatted = comparison.getEstimatedProfitFormatted();

            // then
            assertThat(formatted).isEqualTo("-5,000만");
        }

        @Test
        @DisplayName("0은 '+' 부호를 붙인다")
        void addsPositiveSignForZero() {
            // given
            HouseTypeComparison comparison = HouseTypeComparison.builder()
                    .houseType("84A")
                    .estimatedProfit(0L)
                    .build();

            // when
            String formatted = comparison.getEstimatedProfitFormatted();

            // then
            assertThat(formatted).startsWith("+");
        }

        @Test
        @DisplayName("null이면 '-'를 반환한다")
        void returnsDashForNull() {
            // given
            HouseTypeComparison comparison = HouseTypeComparison.builder()
                    .houseType("84A")
                    .estimatedProfit(null)
                    .build();

            // when
            String formatted = comparison.getEstimatedProfitFormatted();

            // then
            assertThat(formatted).isEqualTo("-");
        }
    }

    @Nested
    @DisplayName("가격 포맷 테스트")
    class PriceFormatting {

        @Test
        @DisplayName("1억 이상은 '억' 단위로 표시한다")
        void formatsOverOneEok() {
            // given
            HouseTypeComparison comparison = HouseTypeComparison.builder()
                    .supplyPrice(52000L)
                    .marketPrice(70000L)
                    .build();

            // then
            assertThat(comparison.getSupplyPriceFormatted()).isEqualTo("5억 2,000만");
            assertThat(comparison.getMarketPriceFormatted()).isEqualTo("7억");
        }

        @Test
        @DisplayName("1억 미만은 '만' 단위로 표시한다")
        void formatsUnderOneEok() {
            // given
            HouseTypeComparison comparison = HouseTypeComparison.builder()
                    .supplyPrice(8500L)
                    .build();

            // then
            assertThat(comparison.getSupplyPriceFormatted()).isEqualTo("8,500만");
        }

        @Test
        @DisplayName("시세가 없으면 '거래 없음'을 반환한다")
        void returnsNoTransactionForNullMarketPrice() {
            // given
            HouseTypeComparison comparison = HouseTypeComparison.builder()
                    .marketPrice(null)
                    .build();

            // then
            assertThat(comparison.getMarketPriceFormatted()).isEqualTo("거래 없음");
        }
    }

    @Nested
    @DisplayName("상태 판단 메서드")
    class StatusMethods {

        @Test
        @DisplayName("양수 차익이면 hasProfit()은 true")
        void hasProfitReturnsTrueForPositive() {
            // given
            HouseTypeComparison comparison = HouseTypeComparison.builder()
                    .estimatedProfit(10000L)
                    .build();

            // then
            assertThat(comparison.hasProfit()).isTrue();
        }

        @Test
        @DisplayName("0 또는 음수 차익이면 hasProfit()은 false")
        void hasProfitReturnsFalseForZeroOrNegative() {
            // given
            HouseTypeComparison zero = HouseTypeComparison.builder()
                    .estimatedProfit(0L)
                    .build();
            HouseTypeComparison negative = HouseTypeComparison.builder()
                    .estimatedProfit(-5000L)
                    .build();

            // then
            assertThat(zero.hasProfit()).isFalse();
            assertThat(negative.hasProfit()).isFalse();
        }

        @Test
        @DisplayName("시세 데이터가 있으면 hasMarketData()는 true")
        void hasMarketDataReturnsTrueWhenExists() {
            // given
            HouseTypeComparison comparison = HouseTypeComparison.builder()
                    .marketPrice(50000L)
                    .build();

            // then
            assertThat(comparison.hasMarketData()).isTrue();
        }
    }

    @Nested
    @DisplayName("getTransactionInfo() - 거래 정보 문자열")
    class GetTransactionInfo {

        @Test
        @DisplayName("거래 내역이 있으면 건수를 표시한다")
        void showsTransactionCount() {
            // given
            List<RealTransaction> transactions = List.of(
                    RealTransaction.builder().aptName("아파트1").build(),
                    RealTransaction.builder().aptName("아파트2").build(),
                    RealTransaction.builder().aptName("아파트3").build()
            );
            HouseTypeComparison comparison = HouseTypeComparison.builder()
                    .similarTransactions(transactions)
                    .build();

            // then
            assertThat(comparison.getTransactionInfo()).isEqualTo("최근 3건 거래 기준");
        }

        @Test
        @DisplayName("거래 내역이 없으면 null을 반환한다")
        void returnsNullForEmptyTransactions() {
            // given
            HouseTypeComparison comparison = HouseTypeComparison.builder()
                    .similarTransactions(List.of())
                    .build();

            // then
            assertThat(comparison.getTransactionInfo()).isNull();
        }
    }
}
