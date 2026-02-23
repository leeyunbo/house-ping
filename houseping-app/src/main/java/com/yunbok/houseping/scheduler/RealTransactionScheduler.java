package com.yunbok.houseping.scheduler;

import com.yunbok.houseping.core.service.realtransaction.RealTransactionCollectionService;
import com.yunbok.houseping.infrastructure.api.SchedulerErrorSlackClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 실거래가 데이터 수집 스케줄러
 * 접수예정 청약 지역의 실거래가를 미리 수집하여 캐싱
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RealTransactionScheduler {

    private final RealTransactionCollectionService realTransactionCollectionService;
    private final SchedulerErrorSlackClient errorNotifier;

    /**
     * 매일 새벽 4시에 접수예정 청약 지역의 실거래가 수집
     */
    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
    public void collectRealTransactions() {
        log.info("[실거래가 스케줄러] 수집 시작");
        try {
            realTransactionCollectionService.collectRealTransactions();
        } catch (Exception e) {
            log.error("[실거래가 스케줄러] 수집 중 오류", e);
            errorNotifier.sendError("실거래가 수집", e);
        }
    }
}
