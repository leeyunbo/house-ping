package com.yunbok.houseping.scheduler;

import com.yunbok.houseping.core.service.subscription.SubscriptionManagementService;
import com.yunbok.houseping.adapter.api.ApplyhomeApiAdapter;
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

import static org.mockito.Mockito.*;

@DisplayName("SubscriptionScheduler - 청약 정보 스케줄러")
@ExtendWith(MockitoExtension.class)
class SubscriptionSchedulerTest {

    @Mock
    private SubscriptionManagementService managementUseCase;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionPriceRepository priceRepository;

    @Mock
    private ApplyhomeApiAdapter applyhomeApiAdapter;

    private SubscriptionScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new SubscriptionScheduler(
                managementUseCase,
                subscriptionRepository,
                priceRepository,
                applyhomeApiAdapter
        );
    }

    @Nested
    @DisplayName("syncRecentData() - 데이터 동기화 (매일 3시)")
    class SyncRecentData {

        @Test
        @DisplayName("정상적으로 동기화를 수행한다")
        void performsSync() {
            // given
            when(managementUseCase.sync()).thenReturn(new SyncResult(10, 5, 3));

            // when
            scheduler.syncRecentData();

            // then
            verify(managementUseCase).sync();
        }
    }

    @Nested
    @DisplayName("cleanupOldData() - 오래된 데이터 정리 (매월 1일 2시)")
    class CleanupOldData {

        @Test
        @DisplayName("오래된 데이터를 삭제한다")
        void deletesOldData() {
            // given
            when(managementUseCase.cleanup()).thenReturn(100);

            // when
            scheduler.cleanupOldData();

            // then
            verify(managementUseCase).cleanup();
        }
    }
}
