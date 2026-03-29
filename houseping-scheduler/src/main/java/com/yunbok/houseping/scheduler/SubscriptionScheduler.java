package com.yunbok.houseping.scheduler;

import com.yunbok.houseping.core.service.subscription.SubscriptionManagementService;
import com.yunbok.houseping.support.CacheNames;
import com.yunbok.houseping.support.annotation.SchedulerMonitor;
import com.yunbok.houseping.support.dto.SchedulerResult;
import com.yunbok.houseping.support.dto.SyncResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionScheduler {

    private final SubscriptionManagementService subscriptionManagementService;
    private final CacheManager cacheManager;

    @SchedulerMonitor("청약 동기화")
    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    public SchedulerResult syncRecentData() {
        SyncResult syncResult = subscriptionManagementService.sync();
        SchedulerResult priceResult = subscriptionManagementService.collectPriceData();

        Objects.requireNonNull(cacheManager.getCache(CacheNames.PRICE_BADGE)).clear();

        return SchedulerResult.of(
                syncResult.inserted() + syncResult.updated() + priceResult.successCount(),
                syncResult.skipped() + priceResult.failCount(),
                priceResult.failureDetails()
        );
    }

    @SchedulerMonitor("오래된 데이터 정리")
    @Scheduled(cron = "0 0 2 1 * *", zone = "Asia/Seoul")
    public SchedulerResult cleanupOldData() {
        int deleted = subscriptionManagementService.cleanup();
        return SchedulerResult.success(deleted);
    }
}
