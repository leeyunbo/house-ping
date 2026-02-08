package com.yunbok.houseping.adapter.in.web;

import com.yunbok.houseping.controller.GlobalExceptionHandler;

import com.yunbok.houseping.support.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("예외 메시지를 응답에 포함한다")
    void includesExceptionMessage() {
        // given
        String errorMessage = "Something went wrong";
        Exception exception = new RuntimeException(errorMessage);

        // when
        ResponseEntity<ApiResponse<Void>> response = handler.handleException(exception);

        // then
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo(errorMessage);
    }

    @Test
    @DisplayName("다양한 예외 타입을 처리한다")
    void handlesVariousExceptionTypes() {
        // given
        Exception illegalArg = new IllegalArgumentException("Invalid argument");
        Exception illegalState = new IllegalStateException("Invalid state");
        Exception nullPointer = new NullPointerException("Null value");

        // when & then
        assertThat(handler.handleException(illegalArg).getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(handler.handleException(illegalState).getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(handler.handleException(nullPointer).getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
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
