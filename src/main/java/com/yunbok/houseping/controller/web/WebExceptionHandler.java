package com.yunbok.houseping.controller.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * 웹 컨트롤러 전용 예외 처리
 * GlobalExceptionHandler(REST JSON)보다 우선 적용
 */
@Slf4j
@ControllerAdvice(basePackages = "com.yunbok.houseping.controller.web")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WebExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException e) {
        log.warn("잘못된 요청: {}", e.getMessage());
        return "redirect:/home";
    }
}
