package com.yunbok.houseping.domain.service;

import com.yunbok.houseping.domain.model.ApplyHomeSubscriptionInfo;
import com.yunbok.houseping.domain.model.SubscriptionConfig;
import com.yunbok.houseping.domain.model.SubscriptionInfo;
import com.yunbok.houseping.domain.model.SyncResult;
import com.yunbok.houseping.domain.port.out.SubscriptionPersistencePort;
import com.yunbok.houseping.domain.port.out.SubscriptionProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("SubscriptionManagementService - 청약 관리 서비스")
@ExtendWith(MockitoExtension.class)
class SubscriptionManagementServiceTest {

    @Mock
    private SubscriptionPersistencePort persistencePort;

    @Mock
    private SubscriptionProvider mockProvider;

    private SubscriptionManagementService service;

    @BeforeEach
    void setUp() {
        List<SubscriptionProvider> providers = List.of(mockProvider);
        SubscriptionConfig config = new SubscriptionConfig(List.of("서울", "경기"));
        service = new SubscriptionManagementService(persistencePort, providers, config);
    }

    @Nested
    @DisplayName("sync() - 데이터 동기화")
    class Sync {

        @Test
        @DisplayName("신규 데이터를 저장하고 inserted 카운트를 증가시킨다")
        void insertsNewData() {
            // given
            when(mockProvider.getSourceName()).thenReturn("TEST_API");
            when(mockProvider.fetchAll("서울")).thenReturn(List.of(createSubscriptionInfo("서울 아파트")));
            when(mockProvider.fetchAll("경기")).thenReturn(List.of());
            when(persistencePort.findBySourceAndHouseNameAndReceiptStartDate(anyString(), anyString(), any()))
                    .thenReturn(Optional.empty());

            // when
            SyncResult result = service.sync();

            // then
            assertThat(result.inserted()).isEqualTo(1);
            verify(persistencePort).save(any(SubscriptionInfo.class), anyString());
        }

        @Test
        @DisplayName("이미 존재하는 데이터는 업데이트한다")
        void updatesExistingData() {
            // given
            SubscriptionInfo existing = createSubscriptionInfo("기존 아파트");

            when(mockProvider.getSourceName()).thenReturn("TEST_API");
            when(mockProvider.fetchAll("서울")).thenReturn(List.of(createSubscriptionInfo("기존 아파트")));
            when(mockProvider.fetchAll("경기")).thenReturn(List.of());
            when(persistencePort.findBySourceAndHouseNameAndReceiptStartDate(anyString(), anyString(), any()))
                    .thenReturn(Optional.of(existing));

            // when
            SyncResult result = service.sync();

            // then
            assertThat(result.updated()).isEqualTo(1);
            verify(persistencePort).update(any(SubscriptionInfo.class), anyString());
        }

        @Test
        @DisplayName("프로바이더 호출 실패 시 해당 지역은 스킵하고 계속 진행한다")
        void continuesOnProviderFailure() {
            // given
            when(mockProvider.getSourceName()).thenReturn("TEST_API");
            when(mockProvider.fetchAll("서울")).thenThrow(new RuntimeException("API 오류"));
            when(mockProvider.fetchAll("경기")).thenReturn(List.of(createSubscriptionInfo("경기 아파트")));
            when(persistencePort.findBySourceAndHouseNameAndReceiptStartDate(anyString(), anyString(), any()))
                    .thenReturn(Optional.empty());

            // when
            SyncResult result = service.sync();

            // then
            assertThat(result.inserted()).isEqualTo(1);
        }

        @Test
        @DisplayName("모든 대상 지역에서 데이터를 수집한다")
        void collectsFromAllTargetAreas() {
            // given
            when(mockProvider.getSourceName()).thenReturn("TEST_API");
            when(mockProvider.fetchAll(anyString())).thenReturn(List.of());

            // when
            service.sync();

            // then
            verify(mockProvider).fetchAll("서울");
            verify(mockProvider).fetchAll("경기");
        }
    }

    @Nested
    @DisplayName("cleanup() - 오래된 데이터 정리")
    class Cleanup {

        @Test
        @DisplayName("1년 이상 지난 데이터를 삭제한다")
        void deletesDataOlderThanOneYear() {
            // given
            when(persistencePort.deleteOldSubscriptions(any())).thenReturn(10);

            // when
            int deletedCount = service.cleanup();

            // then
            assertThat(deletedCount).isEqualTo(10);
            verify(persistencePort).deleteOldSubscriptions(any(LocalDate.class));
        }
    }

    private SubscriptionInfo createSubscriptionInfo(String houseName) {
        return ApplyHomeSubscriptionInfo.builder()
                .houseName(houseName)
                .area("서울")
                .announceDate(LocalDate.of(2025, 1, 1))
                .receiptStartDate(LocalDate.of(2025, 1, 15))
                .receiptEndDate(LocalDate.of(2025, 1, 20))
                .detailUrl("https://example.com")
                .build();
    }
}
