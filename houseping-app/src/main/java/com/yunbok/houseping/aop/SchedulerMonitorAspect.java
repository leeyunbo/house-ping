package com.yunbok.houseping.aop;

import com.yunbok.houseping.externalapi.SchedulerErrorSlackClient;
import com.yunbok.houseping.support.annotation.SchedulerMonitor;
import com.yunbok.houseping.support.dto.SchedulerResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class SchedulerMonitorAspect {

    private final SchedulerErrorSlackClient errorNotifier;

    @Around("@annotation(monitor)")
    public Object monitor(ProceedingJoinPoint joinPoint, SchedulerMonitor monitor) throws Throwable {
        String taskName = monitor.value();
        long start = System.currentTimeMillis();

        log.info("[scheduler] {} 시작", taskName);

        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;

            if (result instanceof SchedulerResult sr) {
                log.info("[scheduler] {} 완료 ({}ms) - {}", taskName, elapsed, sr.summary());
                if (sr.hasFailed()) {
                    errorNotifier.sendWarning(taskName, sr.summary());
                }
            } else {
                log.info("[scheduler] {} 완료 ({}ms)", taskName, elapsed);
            }

            return result;
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("[scheduler] {} 실패 ({}ms)", taskName, elapsed, e);
            errorNotifier.sendError(taskName, e);
            throw e;
        }
    }
}
