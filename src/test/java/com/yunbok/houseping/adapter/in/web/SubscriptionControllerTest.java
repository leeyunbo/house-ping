package com.yunbok.houseping.adapter.in.web;

import com.yunbok.houseping.adapter.in.web.dto.*;
import com.yunbok.houseping.domain.model.ApplyHomeSubscriptionInfo;
import com.yunbok.houseping.domain.model.SubscriptionInfo;
import com.yunbok.houseping.domain.model.SyncResult;
import com.yunbok.houseping.domain.port.in.SubscriptionManagementUseCase;
import com.yunbok.houseping.domain.port.in.SubscriptionUseCase;
import com.yunbok.houseping.domain.port.out.SubscriptionMessageFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@DisplayName("SubscriptionController - 청약 API 컨트롤러")
@ExtendWith(MockitoExtension.class)
class SubscriptionControllerTest {

    @Mock
    private SubscriptionUseCase subscriptionUseCase;

    @Mock
    private SubscriptionManagementUseCase managementUseCase;

    @Mock
    private SubscriptionMessageFormatter messageFormatter;

    private SubscriptionController controller;

    @BeforeEach
    void setUp() {
        controller = new SubscriptionController(subscriptionUseCase, managementUseCase, messageFormatter);
    }

    @Nested
    @DisplayName("collect() - 청약 수집 실행")
    class Collect {

        @Test
        @DisplayName("수집 성공 시 수집 건수를 반환한다")
        void returnsCollectCount() {
            // given
            List<SubscriptionInfo> subscriptions = List.of(
                    createSubscription("힐스테이트 강남"),
                    createSubscription("래미안 판교")
            );
            when(subscriptionUseCase.collect(any(LocalDate.class), eq(true)))
                    .thenReturn(subscriptions);

            // when
            ApiResponse<CollectResponse> response = controller.collect();

            // then
            assertThat(response.success()).isTrue();
            assertThat(response.data().newSubscriptionsCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("test() - 테스트 수집 (알림 없음)")
    class TestCollect {

        @Test
        @DisplayName("특정 날짜의 청약 정보를 조회한다")
        void returnsSubscriptionsForDate() {
            // given
            LocalDate testDate = LocalDate.of(2025, 1, 15);
            List<SubscriptionInfo> subscriptions = List.of(createSubscription("테스트 아파트"));

            when(subscriptionUseCase.collect(testDate, false)).thenReturn(subscriptions);
            when(messageFormatter.formatBatchSummary(any())).thenReturn("요약 메시지");
            when(messageFormatter.formatSubscription(any())).thenReturn("상세 메시지");

            // when
            ApiResponse<TestCollectResponse> response = controller.test(testDate);

            // then
            assertThat(response.success()).isTrue();
            assertThat(response.data().date()).isEqualTo(testDate);
            assertThat(response.data().subscriptionsCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("청약 정보가 없으면 빈 미리보기 메시지를 반환한다")
        void returnsNoDataMessageWhenEmpty() {
            // given
            LocalDate testDate = LocalDate.of(2025, 1, 15);
            when(subscriptionUseCase.collect(testDate, false)).thenReturn(List.of());
            when(messageFormatter.formatNoDataMessage()).thenReturn("신규 정보 없음");

            // when
            ApiResponse<TestCollectResponse> response = controller.test(testDate);

            // then
            assertThat(response.success()).isTrue();
            assertThat(response.data().subscriptionsCount()).isZero();
        }
    }

    @Nested
    @DisplayName("syncInitial() - 초기 동기화")
    class SyncInitial {

        @Test
        @DisplayName("동기화 성공 시 결과를 반환한다")
        void returnsSyncResult() {
            // given
            SyncResult result = new SyncResult(10, 5, 3);
            when(managementUseCase.sync()).thenReturn(result);

            // when
            ApiResponse<SyncResponse> response = controller.syncInitial();

            // then
            assertThat(response.success()).isTrue();
            assertThat(response.data().inserted()).isEqualTo(10);
            assertThat(response.data().updated()).isEqualTo(5);
            assertThat(response.data().skipped()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("cleanup() - 오래된 데이터 정리")
    class Cleanup {

        @Test
        @DisplayName("정리 성공 시 삭제 건수를 반환한다")
        void returnsDeletedCount() {
            // given
            when(managementUseCase.cleanup()).thenReturn(50);

            // when
            ApiResponse<CleanupResponse> response = controller.cleanup();

            // then
            assertThat(response.success()).isTrue();
            assertThat(response.data().deletedCount()).isEqualTo(50);
        }
    }

    private SubscriptionInfo createSubscription(String houseName) {
        return ApplyHomeSubscriptionInfo.builder()
                .houseName(houseName)
                .area("서울")
                .build();
    }
}
