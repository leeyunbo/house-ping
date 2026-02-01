package com.yunbok.houseping.adapter.in.scheduler;

import com.yunbok.houseping.domain.model.SyncResult;
import com.yunbok.houseping.domain.port.in.SubscriptionManagementUseCase;
import com.yunbok.houseping.domain.port.in.SubscriptionUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("SubscriptionScheduler - 청약 정보 스케줄러")
@ExtendWith(MockitoExtension.class)
class SubscriptionSchedulerTest {

    @Mock
    private SubscriptionUseCase subscriptionUseCase;

    @Mock
    private SubscriptionManagementUseCase managementUseCase;

    private SubscriptionScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new SubscriptionScheduler(subscriptionUseCase, managementUseCase);
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

    // DailyNotificationScheduler로 통합되어 비활성화
    // @Nested
    // @DisplayName("collectDailySubscriptions() - 일일 청약 수집 (매일 9시)")
    // class CollectDailySubscriptions {
    //
    //     @Test
    //     @DisplayName("청약 정보 수집 및 알림을 수행한다")
    //     void collectsAndNotifies() {
    //         // given
    //         when(subscriptionUseCase.collect(any(LocalDate.class), eq(true)))
    //                 .thenReturn(List.of());
    //
    //         // when
    //         scheduler.collectDailySubscriptions();
    //
    //         // then
    //         verify(subscriptionUseCase).collect(any(LocalDate.class), eq(true));
    //     }
    // }

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
