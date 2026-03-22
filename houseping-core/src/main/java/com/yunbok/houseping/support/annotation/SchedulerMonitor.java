package com.yunbok.houseping.support.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 스케줄러 메서드에 적용하면 AOP가 자동으로:
 * - 시작/종료 로그
 * - 실행 시간 측정
 * - 예외 → Slack 에러 알림
 * - SchedulerResult.hasFailed() → Slack 경고 알림
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SchedulerMonitor {

    /**
     * 스케줄러 작업 이름 (알림/로그에 표시)
     */
    String value();
}
