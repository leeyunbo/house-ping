package com.yunbok.houseping.scheduler;

import com.yunbok.houseping.core.service.realtransaction.RealTransactionCollectionService;
import com.yunbok.houseping.support.CacheNames;
import com.yunbok.houseping.support.annotation.SchedulerMonitor;
import com.yunbok.houseping.support.dto.SchedulerResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class RealTransactionScheduler {

    private final RealTransactionCollectionService realTransactionCollectionService;
    private final CacheManager cacheManager;

    @SchedulerMonitor("실거래가 수집")
    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
    public SchedulerResult collectRealTransactions() {
        int failCount = realTransactionCollectionService.collectRealTransactions();
        Objects.requireNonNull(cacheManager.getCache(CacheNames.PRICE_BADGE)).clear();
        return SchedulerResult.of(0, failCount);
    }
}
