package com.yunbok.houseping.core.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CompetitionRate - 청약 경쟁률 도메인 모델")
class CompetitionRateTest {

    @Nested
    @DisplayName("getEffectiveRate() - 유효 경쟁률 반환")
    class GetEffectiveRate {

        @Test
        @DisplayName("경쟁률이 설정되어 있으면 그대로 반환한다")
        void returnsCompetitionRateWhenSet() {
            // given
            CompetitionRate rate = CompetitionRate.builder()
                    .competitionRate(new BigDecimal("7.50"))
                    .supplyCount(10)
                    .requestCount(50)
                    .build();

            // when
            BigDecimal result = rate.getEffectiveRate();

            // then
            assertThat(result).isEqualByComparingTo(new BigDecimal("7.50"));
        }

        @Test
        @DisplayName("경쟁률이 null이면 접수건수/공급세대수로 계산한다")
        void calculatesFromRequestAndSupply() {
            // given
            CompetitionRate rate = CompetitionRate.builder()
                    .competitionRate(null)
                    .supplyCount(10)
                    .requestCount(50)
                    .build();

            // when
            BigDecimal result = rate.getEffectiveRate();

            // then
            assertThat(result).isEqualByComparingTo(new BigDecimal("5.00"));
        }

        @Test
        @DisplayName("경쟁률이 null이면 소수점 둘째자리까지 반올림한다")
        void roundsToTwoDecimalPlaces() {
            // given
            CompetitionRate rate = CompetitionRate.builder()
                    .competitionRate(null)
                    .supplyCount(3)
                    .requestCount(10)
                    .build();

            // when
            BigDecimal result = rate.getEffectiveRate();

            // then
            assertThat(result).isEqualByComparingTo(new BigDecimal("3.33"));
        }

        @Test
        @DisplayName("공급세대수가 0이면 null을 반환한다")
        void returnsNullWhenSupplyIsZero() {
            // given
            CompetitionRate rate = CompetitionRate.builder()
                    .competitionRate(null)
                    .supplyCount(0)
                    .requestCount(50)
                    .build();

            // when
            BigDecimal result = rate.getEffectiveRate();

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("공급세대수가 null이면 null을 반환한다")
        void returnsNullWhenSupplyIsNull() {
            // given
            CompetitionRate rate = CompetitionRate.builder()
                    .competitionRate(null)
                    .supplyCount(null)
                    .requestCount(50)
                    .build();

            // when
            BigDecimal result = rate.getEffectiveRate();

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("접수건수가 null이면 null을 반환한다")
        void returnsNullWhenRequestIsNull() {
            // given
            CompetitionRate rate = CompetitionRate.builder()
                    .competitionRate(null)
                    .supplyCount(10)
                    .requestCount(null)
                    .build();

            // when
            BigDecimal result = rate.getEffectiveRate();

            // then
            assertThat(result).isNull();
        }
    }
}
