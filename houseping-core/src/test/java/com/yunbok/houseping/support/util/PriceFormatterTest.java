package com.yunbok.houseping.support.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PriceFormatter - 가격 포맷팅 유틸")
class PriceFormatterTest {

    @Nested
    @DisplayName("format() - 금액을 억/만 단위로 포맷팅")
    class Format {

        @Test
        @DisplayName("1억 이상이고 나머지가 0이면 'X억'으로 포맷팅한다")
        void formatsExactUk() {
            // when
            String result = PriceFormatter.format(50000);

            // then
            assertThat(result).isEqualTo("5억");
        }

        @Test
        @DisplayName("1억 이상이고 나머지가 있으면 'X억 Y만'으로 포맷팅한다")
        void formatsUkWithRemainder() {
            // when
            String result = PriceFormatter.format(52300);

            // then
            assertThat(result).isEqualTo("5억 2,300만");
        }

        @Test
        @DisplayName("1억 미만이면 'X만'으로 포맷팅한다")
        void formatsManOnly() {
            // when
            String result = PriceFormatter.format(9999);

            // then
            assertThat(result).isEqualTo("9,999만");
        }

        @Test
        @DisplayName("정확히 1억이면 '1억'으로 포맷팅한다")
        void formatsExactlyOneUk() {
            // when
            String result = PriceFormatter.format(10000);

            // then
            assertThat(result).isEqualTo("1억");
        }
    }

    @Nested
    @DisplayName("formatWithWon() - 금액을 억/만 원 단위로 포맷팅")
    class FormatWithWon {

        @Test
        @DisplayName("1억 이상이고 나머지가 0이면 'X억 원'으로 포맷팅한다")
        void formatsExactUkWithWon() {
            // when
            String result = PriceFormatter.formatWithWon(50000);

            // then
            assertThat(result).isEqualTo("5억 원");
        }

        @Test
        @DisplayName("1억 이상이고 나머지가 있으면 'X억 Y만 원'으로 포맷팅한다")
        void formatsUkWithRemainderAndWon() {
            // when
            String result = PriceFormatter.formatWithWon(52300);

            // then
            assertThat(result).isEqualTo("5억 2,300만 원");
        }

        @Test
        @DisplayName("1억 미만이면 'X만 원'으로 포맷팅한다")
        void formatsManOnlyWithWon() {
            // when
            String result = PriceFormatter.formatWithWon(9999);

            // then
            assertThat(result).isEqualTo("9,999만 원");
        }
    }
}
