package com.yunbok.houseping.support.exception;

import org.springframework.http.HttpStatus;

public class ExternalApiException extends BusinessException {

    public ExternalApiException(String message) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE, "EXTERNAL_API_ERROR");
    }

    public ExternalApiException(String source, Throwable cause) {
        super(source + " API 호출 중 오류가 발생했습니다", HttpStatus.SERVICE_UNAVAILABLE, "EXTERNAL_API_ERROR");
        initCause(cause);
    }
}
