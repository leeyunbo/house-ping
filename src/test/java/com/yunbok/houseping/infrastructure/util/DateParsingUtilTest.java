package com.yunbok.houseping.infrastructure.util;

import com.yunbok.houseping.support.util.DateParsingUtil;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DateParsingUtil - 날짜 파싱 유틸리티")
class DateParsingUtilTest {

    @Nested
    @DisplayName("parse() - 날짜 파싱")
    class Parse {

        @Test
        @DisplayName("yyyyMMdd 형식을 파싱한다")
        void parsesYyyymmddFormat() {
            // when
            LocalDate result = DateParsingUtil.parse("20250115");

            // then
            assertThat(result).isEqualTo(LocalDate.of(2025, 1, 15));
        }

        @Test
        @DisplayName("yyyy.MM.dd 형식을 파싱한다")
        void parsesYyyyDotMmDotDdFormat() {
            // when
            LocalDate result = DateParsingUtil.parse("2025.01.15");

            // then
            assertThat(result).isEqualTo(LocalDate.of(2025, 1, 15));
        }

        @Test
        @DisplayName("yyyy-MM-dd 형식을 파싱한다")
        void parsesYyyyDashMmDashDdFormat() {
            // when
            LocalDate result = DateParsingUtil.parse("2025-01-15");

            // then
            assertThat(result).isEqualTo(LocalDate.of(2025, 1, 15));
        }

        @Test
        @DisplayName("yyyyMMddHHmm 형식을 파싱한다")
        void parsesDateTimeFormat() {
            // when
            LocalDate result = DateParsingUtil.parse("202501150930");

            // then
            assertThat(result).isEqualTo(LocalDate.of(2025, 1, 15));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"-", "   ", "  -  "})
        @DisplayName("빈 값이나 대시는 null을 반환한다")
        void returnsNullForBlankOrDash(String input) {
            // when
            LocalDate result = DateParsingUtil.parse(input);

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("잘못된 형식은 null을 반환한다")
        void returnsNullForInvalidFormat() {
            // when
            LocalDate result = DateParsingUtil.parse("invalid-date");

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("앞뒤 공백을 제거하고 파싱한다")
        void trimsWhitespace() {
            // when
            LocalDate result = DateParsingUtil.parse("  20250115  ");

            // then
            assertThat(result).isEqualTo(LocalDate.of(2025, 1, 15));
        }
    }

    @Nested
    @DisplayName("isBlankOrDash() - 빈 값/대시 확인")
    class IsBlankOrDash {

        @Test
        @DisplayName("null은 true를 반환한다")
        void returnsTrueForNull() {
            assertThat(DateParsingUtil.isBlankOrDash(null)).isTrue();
        }

        @Test
        @DisplayName("빈 문자열은 true를 반환한다")
        void returnsTrueForEmpty() {
            assertThat(DateParsingUtil.isBlankOrDash("")).isTrue();
        }

        @Test
        @DisplayName("공백만 있는 문자열은 true를 반환한다")
        void returnsTrueForWhitespace() {
            assertThat(DateParsingUtil.isBlankOrDash("   ")).isTrue();
        }

        @Test
        @DisplayName("대시는 true를 반환한다")
        void returnsTrueForDash() {
            assertThat(DateParsingUtil.isBlankOrDash("-")).isTrue();
        }

        @Test
        @DisplayName("일반 문자열은 false를 반환한다")
        void returnsFalseForNormalString() {
            assertThat(DateParsingUtil.isBlankOrDash("20250115")).isFalse();
        }
    }
}
