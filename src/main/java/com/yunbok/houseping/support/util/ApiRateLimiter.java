package com.yunbok.houseping.support.util;

import lombok.extern.slf4j.Slf4j;

/**
 * API 호출 간 딜레이를 위한 유틸리티.
 * 추후 Resilience4j RateLimiter로 교체 가능.
 */
@Slf4j
public final class ApiRateLimiter {

    private ApiRateLimiter() {}

    /**
     * API 호출 간 딜레이
     * @param millis 대기 시간 (밀리초)
     */
    public static void delay(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("API rate limit delay interrupted");
        }
    }
}
