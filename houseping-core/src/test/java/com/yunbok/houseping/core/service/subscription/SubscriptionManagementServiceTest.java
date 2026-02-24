package com.yunbok.houseping.core.service.subscription;

import com.yunbok.houseping.core.domain.Subscription;
import com.yunbok.houseping.core.domain.SubscriptionConfig;
import com.yunbok.houseping.core.port.SubscriptionPersistencePort;
import com.yunbok.houseping.support.dto.SyncResult;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("SubscriptionManagementService - 청약 관리 서비스")
@ExtendWith(MockitoExtension.class)
class SubscriptionManagementServiceTest {

    @Mock
    private SubscriptionPersistencePort subscriptionStore;

    @Mock
    private SubscriptionProviderChain chain1;

    @Mock
    private SubscriptionProviderChain chain2;

    private SubscriptionConfig config;

    private SubscriptionManagementService service;

    @BeforeEach
    void setUp() {
        config = new SubscriptionConfig(List.of("서울", "경기"));
    }

    @Nested
    @DisplayName("sync() - 청약 동기화")
    class Sync {

        @Test
        @DisplayName("신규 청약을 저장한다")
        void insertsNewSubscriptions() {
            // given
            service = new SubscriptionManagementService(subscriptionStore, List.of(chain1), config);
            Subscription sub = createSubscription("신규아파트");

            when(chain1.getSourceName()).thenReturn("ApplyHome");
            when(chain1.executeAll("서울")).thenReturn(List.of(sub));
            when(chain1.executeAll("경기")).thenReturn(List.of());
            when(subscriptionStore.findBySourceAndHouseNameAndReceiptStartDate(
                    eq("ApplyHome"), eq("신규아파트"), any())).thenReturn(Optional.empty());

            // when
            SyncResult result = service.sync();

            // then
            assertThat(result.inserted()).isEqualTo(1);
            assertThat(result.updated()).isEqualTo(0);
            verify(subscriptionStore).save(eq(sub), eq("ApplyHome"));
        }

        @Test
        @DisplayName("기존 청약을 업데이트한다")
        void updatesExistingSubscriptions() {
            // given
            service = new SubscriptionManagementService(subscriptionStore, List.of(chain1), config);
            Subscription sub = createSubscription("기존아파트");
            Subscription existing = createSubscription("기존아파트");

            when(chain1.getSourceName()).thenReturn("ApplyHome");
            when(chain1.executeAll("서울")).thenReturn(List.of(sub));
            when(chain1.executeAll("경기")).thenReturn(List.of());
            when(subscriptionStore.findBySourceAndHouseNameAndReceiptStartDate(
                    eq("ApplyHome"), eq("기존아파트"), any())).thenReturn(Optional.of(existing));

            // when
            SyncResult result = service.sync();

            // then
            assertThat(result.inserted()).isEqualTo(0);
            assertThat(result.updated()).isEqualTo(1);
            verify(subscriptionStore).update(eq(sub), eq("ApplyHome"));
        }

        @Test
        @DisplayName("멀티 체인으로 여러 소스를 동기화한다")
        void syncsMultipleChains() {
            // given
            service = new SubscriptionManagementService(subscriptionStore, List.of(chain1, chain2), config);
            Subscription sub1 = createSubscription("ApplyHome아파트");
            Subscription sub2 = createSubscription("LH아파트");

            when(chain1.getSourceName()).thenReturn("ApplyHome");
            when(chain1.executeAll(anyString())).thenReturn(List.of());
            when(chain1.executeAll("서울")).thenReturn(List.of(sub1));

            when(chain2.getSourceName()).thenReturn("LH");
            when(chain2.executeAll(anyString())).thenReturn(List.of());
            when(chain2.executeAll("서울")).thenReturn(List.of(sub2));

            when(subscriptionStore.findBySourceAndHouseNameAndReceiptStartDate(
                    anyString(), anyString(), any())).thenReturn(Optional.empty());

            // when
            SyncResult result = service.sync();

            // then
            assertThat(result.inserted()).isEqualTo(2);
        }

        @Test
        @DisplayName("빈 결과면 0건을 반환한다")
        void returnsEmptyForNoResults() {
            // given
            service = new SubscriptionManagementService(subscriptionStore, List.of(chain1), config);
            when(chain1.executeAll(anyString())).thenReturn(List.of());

            // when
            SyncResult result = service.sync();

            // then
            assertThat(result.inserted()).isEqualTo(0);
            assertThat(result.updated()).isEqualTo(0);
            verify(subscriptionStore, never()).save(any(), anyString());
        }
    }

    @Nested
    @DisplayName("cleanup() - 오래된 청약 삭제")
    class Cleanup {

        @Test
        @DisplayName("삭제된 건수를 반환한다")
        void returnsDeletedCount() {
            // given
            service = new SubscriptionManagementService(subscriptionStore, List.of(), config);
            when(subscriptionStore.deleteOldSubscriptions(any(LocalDate.class))).thenReturn(5);

            // when
            int result = service.cleanup();

            // then
            assertThat(result).isEqualTo(5);
            verify(subscriptionStore).deleteOldSubscriptions(any(LocalDate.class));
        }
    }

    private Subscription createSubscription(String houseName) {
        return Subscription.builder()
                .houseName(houseName)
                .area("서울")
                .receiptStartDate(LocalDate.now())
                .receiptEndDate(LocalDate.now().plusDays(5))
                .build();
    }
}
