package com.yunbok.houseping.infrastructure.adapter.inbound;

import com.yunbok.houseping.domain.port.CollectSubscriptionUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 청약 정보 수집 스케줄러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionScheduler {
    
    private final CollectSubscriptionUseCase collectSubscriptionUseCase;

    /**
     * 매일 오전 9시에 청약 정보 수집
     */
    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
    public void collectDailySubscriptions() {
        log.info("⏰ [스케줄] 일일 청약 정보 수집을 시작합니다.");
        collectSubscriptionUseCase.collectAndNotifyTodaySubscriptions();
    }
}
