package com.yunbok.houseping.infrastructure.util;

import com.yunbok.houseping.support.util.MapExtractor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MapExtractor - Map 값 추출 유틸리티")
class MapExtractorTest {

    @Nested
    @DisplayName("getString() - 문자열 추출")
    class GetString {

        @Test
        @DisplayName("존재하는 문자열을 반환한다")
        void returnsExistingString() {
            // given
            Map<String, Object> map = Map.of("key", "value");

            // when
            String result = MapExtractor.getString(map, "key");

            // then
            assertThat(result).isEqualTo("value");
        }

        @Test
        @DisplayName("null 값은 null을 반환한다")
        void returnsNullForNullValue() {
            // given
            Map<String, Object> map = new HashMap<>();
            map.put("key", null);

            // when
            String result = MapExtractor.getString(map, "key");

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("존재하지 않는 키는 null을 반환한다")
        void returnsNullForMissingKey() {
            // given
            Map<String, Object> map = Map.of("other", "value");

            // when
            String result = MapExtractor.getString(map, "key");

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("빈 문자열은 null을 반환한다")
        void returnsNullForEmptyString() {
            // given
            Map<String, Object> map = Map.of("key", "");

            // when
            String result = MapExtractor.getString(map, "key");

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("대시는 null을 반환한다")
        void returnsNullForDash() {
            // given
            Map<String, Object> map = Map.of("key", "-");

            // when
            String result = MapExtractor.getString(map, "key");

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("앞뒤 공백을 제거한다")
        void trimsWhitespace() {
            // given
            Map<String, Object> map = Map.of("key", "  value  ");

            // when
            String result = MapExtractor.getString(map, "key");

            // then
            assertThat(result).isEqualTo("value");
        }

        @Test
        @DisplayName("숫자도 문자열로 반환한다")
        void convertsNumberToString() {
            // given
            Map<String, Object> map = Map.of("key", 123);

            // when
            String result = MapExtractor.getString(map, "key");

            // then
            assertThat(result).isEqualTo("123");
        }
    }

    @Nested
    @DisplayName("getStringOrEmpty() - 문자열 추출 (기본값 빈 문자열)")
    class GetStringOrEmpty {

        @Test
        @DisplayName("존재하는 문자열을 반환한다")
        void returnsExistingString() {
            // given
            Map<String, Object> map = Map.of("key", "value");

            // when
            String result = MapExtractor.getStringOrEmpty(map, "key");

            // then
            assertThat(result).isEqualTo("value");
        }

        @Test
        @DisplayName("null 값은 빈 문자열을 반환한다")
        void returnsEmptyForNull() {
            // given
            Map<String, Object> map = new HashMap<>();
            map.put("key", null);

            // when
            String result = MapExtractor.getStringOrEmpty(map, "key");

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getInteger() - 정수 추출")
    class GetInteger {

        @Test
        @DisplayName("Number 타입을 정수로 반환한다")
        void returnsIntegerFromNumber() {
            // given
            Map<String, Object> map = Map.of("key", 123);

            // when
            Integer result = MapExtractor.getInteger(map, "key");

            // then
            assertThat(result).isEqualTo(123);
        }

        @Test
        @DisplayName("Double 타입도 정수로 변환한다")
        void convertsDoubleToInteger() {
            // given
            Map<String, Object> map = Map.of("key", 123.9);

            // when
            Integer result = MapExtractor.getInteger(map, "key");

            // then
            assertThat(result).isEqualTo(123);
        }

        @Test
        @DisplayName("문자열 숫자를 정수로 변환한다")
        void parsesStringNumber() {
            // given
            Map<String, Object> map = Map.of("key", "456");

            // when
            Integer result = MapExtractor.getInteger(map, "key");

            // then
            assertThat(result).isEqualTo(456);
        }

        @Test
        @DisplayName("유효하지 않은 문자열은 null을 반환한다")
        void returnsNullForInvalidString() {
            // given
            Map<String, Object> map = Map.of("key", "not-a-number");

            // when
            Integer result = MapExtractor.getInteger(map, "key");

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("null 값은 null을 반환한다")
        void returnsNullForNullValue() {
            // given
            Map<String, Object> map = new HashMap<>();
            map.put("key", null);

            // when
            Integer result = MapExtractor.getInteger(map, "key");

            // then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("getIntegerOrDefault() - 정수 추출 (기본값 지정)")
    class GetIntegerOrDefault {

        @Test
        @DisplayName("존재하는 정수를 반환한다")
        void returnsExistingInteger() {
            // given
            Map<String, Object> map = Map.of("key", 123);

            // when
            int result = MapExtractor.getIntegerOrDefault(map, "key", 0);

            // then
            assertThat(result).isEqualTo(123);
        }

        @Test
        @DisplayName("null 값은 기본값을 반환한다")
        void returnsDefaultForNull() {
            // given
            Map<String, Object> map = new HashMap<>();
            map.put("key", null);

            // when
            int result = MapExtractor.getIntegerOrDefault(map, "key", 999);

            // then
            assertThat(result).isEqualTo(999);
        }

        @Test
        @DisplayName("존재하지 않는 키는 기본값을 반환한다")
        void returnsDefaultForMissingKey() {
            // given
            Map<String, Object> map = Map.of("other", 123);

            // when
            int result = MapExtractor.getIntegerOrDefault(map, "key", 999);

            // then
            assertThat(result).isEqualTo(999);
        }
    }
}
