package com.yunbok.houseping.support.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends BusinessException {

    public NotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "NOT_FOUND");
    }

    public static NotFoundException subscription(Long id) {
        return new NotFoundException("청약 정보를 찾을 수 없습니다: " + id);
    }

    public static NotFoundException user(Long id) {
        return new NotFoundException("사용자를 찾을 수 없습니다: " + id);
    }
}
