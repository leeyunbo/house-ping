package com.yunbok.houseping.infrastructure.adapter.inbound;

import com.yunbok.houseping.domain.port.CollectSubscriptionUseCase;
import com.yunbok.houseping.domain.service.SubscriptionSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 청약 정보 수집 및 동기화 스케줄러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionScheduler {

    private final CollectSubscriptionUseCase collectSubscriptionUseCase;
    private final SubscriptionSyncService subscriptionSyncService;

    /**
     * 매일 새벽 3시: 데이터 동기화
     */
    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    public void syncRecentData() {
        try {
            log.info("📅 [스케줄러] 데이터 동기화 시작 (매일 3시)");
            SubscriptionSyncService.SyncResult result = subscriptionSyncService.syncInitialData();
            log.info("✅ [스케줄러] 데이터 동기화 완료 - 추가: {}, 업데이트: {}, 스킵: {}",
                     result.inserted, result.updated, result.skipped);
        } catch (Exception e) {
            log.error("❌ [스케줄러] 데이터 동기화 실패", e);
        }
    }

    /**
     * 매일 오전 9시: 청약 정보 수집 및 알림
     */
    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
    public void collectDailySubscriptions() {
        log.info("⏰ [스케줄] 일일 청약 정보 수집을 시작합니다.");
        collectSubscriptionUseCase.collectAndNotifyTodaySubscriptions();
    }

    /**
     * 매월 1일 새벽 2시: 오래된 데이터 정리 (1년 이상 지난 데이터)
     */
    @Scheduled(cron = "0 0 2 1 * *", zone = "Asia/Seoul")
    public void cleanupOldData() {
        try {
            log.info("📅 [스케줄러] 오래된 데이터 정리 시작 (매월 1일)");
            int deletedCount = subscriptionSyncService.cleanupOldData();
            log.info("✅ [스케줄러] 오래된 데이터 정리 완료 - {}건 삭제", deletedCount);
        } catch (Exception e) {
            log.error("❌ [스케줄러] 오래된 데이터 정리 실패", e);
        }
    }
}
