package com.yunbok.houseping.support.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ApiResponse - API 응답 DTO")
class ApiResponseTest {

    @Nested
    @DisplayName("success(T data) - 데이터만 포함한 성공 응답")
    class SuccessWithData {

        @Test
        @DisplayName("success가 true이다")
        void successIsTrue() {
            // when
            ApiResponse<String> response = ApiResponse.success("test data");

            // then
            assertThat(response.success()).isTrue();
        }

        @Test
        @DisplayName("message는 null이다")
        void messageIsNull() {
            // when
            ApiResponse<String> response = ApiResponse.success("test data");

            // then
            assertThat(response.message()).isNull();
        }

        @Test
        @DisplayName("data를 포함한다")
        void containsData() {
            // when
            ApiResponse<String> response = ApiResponse.success("test data");

            // then
            assertThat(response.data()).isEqualTo("test data");
        }

        @Test
        @DisplayName("null 데이터도 허용한다")
        void allowsNullData() {
            // when
            ApiResponse<String> response = ApiResponse.success(null);

            // then
            assertThat(response.success()).isTrue();
            assertThat(response.data()).isNull();
        }
    }

    @Nested
    @DisplayName("success(String message, T data) - 메시지와 데이터를 포함한 성공 응답")
    class SuccessWithMessageAndData {

        @Test
        @DisplayName("success가 true이다")
        void successIsTrue() {
            // when
            ApiResponse<String> response = ApiResponse.success("Success message", "test data");

            // then
            assertThat(response.success()).isTrue();
        }

        @Test
        @DisplayName("message를 포함한다")
        void containsMessage() {
            // when
            ApiResponse<String> response = ApiResponse.success("Success message", "test data");

            // then
            assertThat(response.message()).isEqualTo("Success message");
        }

        @Test
        @DisplayName("data를 포함한다")
        void containsData() {
            // when
            ApiResponse<Integer> response = ApiResponse.success("Count retrieved", 42);

            // then
            assertThat(response.data()).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("error(String message) - 에러 응답")
    class Error {

        @Test
        @DisplayName("success가 false이다")
        void successIsFalse() {
            // when
            ApiResponse<Object> response = ApiResponse.error("Error occurred");

            // then
            assertThat(response.success()).isFalse();
        }

        @Test
        @DisplayName("message를 포함한다")
        void containsMessage() {
            // when
            ApiResponse<Object> response = ApiResponse.error("Something went wrong");

            // then
            assertThat(response.message()).isEqualTo("Something went wrong");
        }

        @Test
        @DisplayName("data는 null이다")
        void dataIsNull() {
            // when
            ApiResponse<String> response = ApiResponse.error("Error");

            // then
            assertThat(response.data()).isNull();
        }
    }

    @Test
    @DisplayName("record의 기본 생성자를 통해 직접 생성할 수 있다")
    void canCreateDirectly() {
        // when
        ApiResponse<String> response = new ApiResponse<>(true, "Custom message", "Custom data");

        // then
        assertThat(response.success()).isTrue();
        assertThat(response.message()).isEqualTo("Custom message");
        assertThat(response.data()).isEqualTo("Custom data");
    }
}
