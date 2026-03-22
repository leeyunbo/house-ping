package com.yunbok.houseping.scheduler;

import com.yunbok.houseping.infrastructure.api.ApplyhomeApiClient;
import com.yunbok.houseping.core.domain.SubscriptionSource;
import com.yunbok.houseping.core.service.subscription.SubscriptionManagementService;
import com.yunbok.houseping.entity.SubscriptionEntity;
import com.yunbok.houseping.repository.SubscriptionPriceRepository;
import com.yunbok.houseping.repository.SubscriptionRepository;
import com.yunbok.houseping.support.CacheNames;
import com.yunbok.houseping.support.annotation.SchedulerMonitor;
import com.yunbok.houseping.support.dto.SchedulerResult;
import com.yunbok.houseping.support.dto.SyncResult;
import com.yunbok.houseping.support.util.ApiRateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionScheduler {

    private final SubscriptionManagementService subscriptionManagementService;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPriceRepository priceRepository;
    private final ApplyhomeApiClient applyhomeApiAdapter;
    private final CacheManager cacheManager;

    @SchedulerMonitor("청약 동기화")
    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    public SchedulerResult syncRecentData() {
        // 1단계: 청약 데이터 동기화
        SyncResult syncResult = subscriptionManagementService.sync();

        // 2단계: 청약Home 신규 청약 분양가 수집
        SchedulerResult priceResult = collectApplyHomePriceData();

        // 3단계: 캐시 초기화
        Objects.requireNonNull(cacheManager.getCache(CacheNames.PRICE_BADGE)).clear();

        return SchedulerResult.of(
                syncResult.inserted() + syncResult.updated() + priceResult.successCount(),
                syncResult.skipped() + priceResult.failCount(),
                priceResult.failureDetails()
        );
    }

    private SchedulerResult collectApplyHomePriceData() {
        List<SubscriptionEntity> subscriptions = subscriptionRepository.findAll().stream()
                .filter(s -> SubscriptionSource.APPLYHOME.matches(s.getSource()))
                .filter(s -> s.getHouseManageNo() != null && !s.getHouseManageNo().isEmpty())
                .filter(s -> !priceRepository.existsByHouseManageNo(s.getHouseManageNo()))
                .toList();

        int successCount = 0;
        List<String> failures = new ArrayList<>();

        for (SubscriptionEntity subscription : subscriptions) {
            try {
                applyhomeApiAdapter.fetchAndSavePriceDetails(
                        subscription.getHouseManageNo(),
                        subscription.getPblancNo(),
                        subscription.getHouseType());
                successCount++;
                ApiRateLimiter.delay(100);
            } catch (Exception e) {
                failures.add(subscription.getHouseName() + ": " + e.getMessage());
            }
        }

        return SchedulerResult.of(successCount, failures.size(), failures);
    }

    @SchedulerMonitor("오래된 데이터 정리")
    @Scheduled(cron = "0 0 2 1 * *", zone = "Asia/Seoul")
    public SchedulerResult cleanupOldData() {
        int deleted = subscriptionManagementService.cleanup();
        return SchedulerResult.success(deleted);
    }
}
