package com.yunbok.houseping.adapter.in.web;

import com.yunbok.houseping.adapter.in.web.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Request failed", e);
        return ResponseEntity.internalServerError()
                .body(ApiResponse.error(e.getMessage()));
    }
}
