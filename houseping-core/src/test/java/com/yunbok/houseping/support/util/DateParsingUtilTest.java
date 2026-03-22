package com.yunbok.houseping.support.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DateParsingUtil - 날짜 파싱 유틸")
class DateParsingUtilTest {

    @Nested
    @DisplayName("parse() - 문자열을 LocalDate로 파싱")
    class Parse {

        @Test
        @DisplayName("yyyyMMddHHmm 형식을 파싱한다")
        void parsesYyyyMMddHHmm() {
            // given
            String dateStr = "202603221430";

            // when
            LocalDate result = DateParsingUtil.parse(dateStr);

            // then
            assertThat(result).isEqualTo(LocalDate.of(2026, 3, 22));
        }

        @Test
        @DisplayName("yyyy.MM.dd 형식을 파싱한다")
        void parsesYyyyDotMMDotDd() {
            // given
            String dateStr = "2026.03.22";

            // when
            LocalDate result = DateParsingUtil.parse(dateStr);

            // then
            assertThat(result).isEqualTo(LocalDate.of(2026, 3, 22));
        }

        @Test
        @DisplayName("yyyyMMdd 형식을 파싱한다")
        void parsesYyyyMMdd() {
            // given
            String dateStr = "20260322";

            // when
            LocalDate result = DateParsingUtil.parse(dateStr);

            // then
            assertThat(result).isEqualTo(LocalDate.of(2026, 3, 22));
        }

        @Test
        @DisplayName("yyyy-MM-dd 형식을 파싱한다")
        void parsesYyyyDashMMDashDd() {
            // given
            String dateStr = "2026-03-22";

            // when
            LocalDate result = DateParsingUtil.parse(dateStr);

            // then
            assertThat(result).isEqualTo(LocalDate.of(2026, 3, 22));
        }

        @Test
        @DisplayName("null을 입력하면 null을 반환한다")
        void returnsNullForNull() {
            // when
            LocalDate result = DateParsingUtil.parse(null);

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("빈 문자열을 입력하면 null을 반환한다")
        void returnsNullForBlank() {
            // when
            LocalDate result = DateParsingUtil.parse("   ");

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("대시(-)를 입력하면 null을 반환한다")
        void returnsNullForDash() {
            // when
            LocalDate result = DateParsingUtil.parse("-");

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("잘못된 형식을 입력하면 null을 반환한다")
        void returnsNullForInvalidFormat() {
            // when
            LocalDate result = DateParsingUtil.parse("not-a-date");

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("앞뒤 공백을 제거하고 파싱한다")
        void trimsWhitespace() {
            // given
            String dateStr = "  20260322  ";

            // when
            LocalDate result = DateParsingUtil.parse(dateStr);

            // then
            assertThat(result).isEqualTo(LocalDate.of(2026, 3, 22));
        }
    }

    @Nested
    @DisplayName("isBlankOrDash() - null/빈값/대시 판별")
    class IsBlankOrDash {

        @Test
        @DisplayName("null이면 true를 반환한다")
        void returnsTrueForNull() {
            assertThat(DateParsingUtil.isBlankOrDash(null)).isTrue();
        }

        @Test
        @DisplayName("빈 문자열이면 true를 반환한다")
        void returnsTrueForEmpty() {
            assertThat(DateParsingUtil.isBlankOrDash("")).isTrue();
        }

        @Test
        @DisplayName("공백 문자열이면 true를 반환한다")
        void returnsTrueForBlank() {
            assertThat(DateParsingUtil.isBlankOrDash("   ")).isTrue();
        }

        @Test
        @DisplayName("대시(-)이면 true를 반환한다")
        void returnsTrueForDash() {
            assertThat(DateParsingUtil.isBlankOrDash("-")).isTrue();
        }

        @Test
        @DisplayName("일반 문자열이면 false를 반환한다")
        void returnsFalseForNormalString() {
            assertThat(DateParsingUtil.isBlankOrDash("20260322")).isFalse();
        }
    }
}
