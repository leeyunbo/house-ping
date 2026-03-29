package com.yunbok.houseping.scheduler;

import com.yunbok.houseping.core.service.subscription.SubscriptionManagementService;
import com.yunbok.houseping.support.CacheNames;
import com.yunbok.houseping.support.dto.SchedulerResult;
import com.yunbok.houseping.support.dto.SyncResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("SubscriptionScheduler - 청약 정보 스케줄러")
@ExtendWith(MockitoExtension.class)
class SubscriptionSchedulerTest {

    @Mock
    private SubscriptionManagementService subscriptionManagementService;

    private SubscriptionScheduler scheduler;

    @BeforeEach
    void setUp() {
        CacheManager cacheManager = new ConcurrentMapCacheManager(CacheNames.PRICE_BADGE);
        scheduler = new SubscriptionScheduler(subscriptionManagementService, cacheManager);
    }

    @Nested
    @DisplayName("syncRecentData() - 데이터 동기화")
    class SyncRecentData {

        @Test
        @DisplayName("동기화 + 분양가 수집 결과를 합산하여 반환한다")
        void returnsMergedResult() {
            // given
            when(subscriptionManagementService.sync()).thenReturn(new SyncResult(10, 5, 2));
            when(subscriptionManagementService.collectPriceData())
                    .thenReturn(SchedulerResult.of(3, 1, List.of("실패건")));

            // when
            SchedulerResult result = scheduler.syncRecentData();

            // then
            assertThat(result.successCount()).isEqualTo(18); // 10+5+3
            assertThat(result.failCount()).isEqualTo(3); // 2+1
            verify(subscriptionManagementService).sync();
            verify(subscriptionManagementService).collectPriceData();
        }
    }

    @Nested
    @DisplayName("cleanupOldData() - 오래된 데이터 정리")
    class CleanupOldData {

        @Test
        @DisplayName("삭제 건수를 반환한다")
        void returnsDeletedCount() {
            // given
            when(subscriptionManagementService.cleanup()).thenReturn(100);

            // when
            SchedulerResult result = scheduler.cleanupOldData();

            // then
            assertThat(result.successCount()).isEqualTo(100);
            assertThat(result.hasFailed()).isFalse();
        }
    }
}
