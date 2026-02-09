package com.yunbok.houseping.controller;

import com.yunbok.houseping.support.dto.ApiResponse;
import com.yunbok.houseping.support.exception.BusinessException;
import com.yunbok.houseping.support.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GlobalExceptionHandler - 전역 예외 처리")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Nested
    @DisplayName("handleBusinessException() - 비즈니스 예외")
    class HandleBusinessException {

        @Test
        @DisplayName("BusinessException 발생 시 해당 상태코드와 메시지를 반환한다")
        void returnsBusinessExceptionStatus() {
            // given
            BusinessException exception = new BusinessException("잘못된 요청입니다", HttpStatus.BAD_REQUEST);

            // when
            ResponseEntity<ApiResponse<Void>> response = handler.handleBusinessException(exception);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().message()).isEqualTo("잘못된 요청입니다");
        }

        @Test
        @DisplayName("NotFoundException 발생 시 404를 반환한다")
        void returnsNotFoundStatus() {
            // given
            NotFoundException exception = NotFoundException.subscription(1L);

            // when
            ResponseEntity<ApiResponse<Void>> response = handler.handleBusinessException(exception);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("handleBadRequest() - 잘못된 요청")
    class HandleBadRequest {

        @Test
        @DisplayName("IllegalArgumentException 발생 시 400을 반환한다")
        void returnsForIllegalArgument() {
            // given
            IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

            // when
            ResponseEntity<ApiResponse<Void>> response = handler.handleBadRequest(exception);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().message()).isEqualTo("잘못된 요청입니다");
        }
    }

    @Nested
    @DisplayName("handleException() - 예상치 못한 예외")
    class HandleException {

        @Test
        @DisplayName("예외 발생 시 500 응답을 반환한다")
        void returnsInternalServerError() {
            // given
            Exception exception = new RuntimeException("Test error message");

            // when
            ResponseEntity<ApiResponse<Void>> response = handler.handleException(exception);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @Test
        @DisplayName("내부 오류 메시지를 노출하지 않는다")
        void doesNotExposeInternalErrorMessage() {
            // given
            Exception exception = new RuntimeException("Internal DB connection failed");

            // when
            ResponseEntity<ApiResponse<Void>> response = handler.handleException(exception);

            // then
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().message()).isEqualTo("서버 오류가 발생했습니다");
            assertThat(response.getBody().message()).doesNotContain("DB");
        }

        @Test
        @DisplayName("null 메시지를 가진 예외도 처리한다")
        void handlesExceptionWithNullMessage() {
            // given
            Exception exception = new RuntimeException((String) null);

            // when
            ResponseEntity<ApiResponse<Void>> response = handler.handleException(exception);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isNotNull();
        }
    }
}
