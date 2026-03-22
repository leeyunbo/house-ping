package com.yunbok.houseping.scheduler;

import com.yunbok.houseping.core.service.subscription.SubscriptionManagementService;
import com.yunbok.houseping.infrastructure.api.ApplyhomeApiClient;
import com.yunbok.houseping.support.dto.SchedulerResult;
import com.yunbok.houseping.support.dto.SyncResult;
import com.yunbok.houseping.repository.SubscriptionPriceRepository;
import com.yunbok.houseping.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("SubscriptionScheduler - 청약 정보 스케줄러")
@ExtendWith(MockitoExtension.class)
class SubscriptionSchedulerTest {

    @Mock
    private SubscriptionManagementService subscriptionManagementService;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionPriceRepository priceRepository;

    @Mock
    private ApplyhomeApiClient applyhomeApiAdapter;

    private SubscriptionScheduler scheduler;

    @BeforeEach
    void setUp() {
        CacheManager cacheManager = new ConcurrentMapCacheManager("priceBadge");
        scheduler = new SubscriptionScheduler(
                subscriptionManagementService,
                subscriptionRepository,
                priceRepository,
                applyhomeApiAdapter,
                cacheManager
        );
    }

    @Nested
    @DisplayName("syncRecentData() - 데이터 동기화")
    class SyncRecentData {

        @Test
        @DisplayName("동기화 결과를 SchedulerResult로 반환한다")
        void returnsSyncResult() {
            // given
            when(subscriptionManagementService.sync()).thenReturn(new SyncResult(10, 5, 2));

            // when
            SchedulerResult result = scheduler.syncRecentData();

            // then
            assertThat(result.successCount()).isGreaterThanOrEqualTo(15);
            assertThat(result.failCount()).isGreaterThanOrEqualTo(2);
            verify(subscriptionManagementService).sync();
        }
    }

    @Nested
    @DisplayName("cleanupOldData() - 오래된 데이터 정리")
    class CleanupOldData {

        @Test
        @DisplayName("오래된 데이터를 삭제한다")
        void deletesOldData() {
            // given
            when(subscriptionManagementService.cleanup()).thenReturn(100);

            // when
            scheduler.cleanupOldData();

            // then
            verify(subscriptionManagementService).cleanup();
        }
    }
}
