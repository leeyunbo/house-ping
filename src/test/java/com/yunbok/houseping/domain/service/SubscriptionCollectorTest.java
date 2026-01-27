package com.yunbok.houseping.domain.service;

import com.yunbok.houseping.domain.model.ApplyHomeSubscriptionInfo;
import com.yunbok.houseping.domain.model.SubscriptionConfig;
import com.yunbok.houseping.domain.model.SubscriptionInfo;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("SubscriptionCollector - 청약 정보 수집기")
@ExtendWith(MockitoExtension.class)
class SubscriptionCollectorTest {

    @Mock
    private SubscriptionProviderChain mockProvider1;

    @Mock
    private SubscriptionProviderChain mockProvider2;

    private SubscriptionCollector collector;

    @BeforeEach
    void setUp() {
        List<SubscriptionProviderChain> chains = List.of(mockProvider1, mockProvider2);
        SubscriptionConfig config = new SubscriptionConfig(List.of("서울", "경기"));
        collector = new SubscriptionCollector(chains, config);
    }

    @Nested
    @DisplayName("collectFromAllAreas() - 전체 지역에서 수집")
    class CollectFromAllAreas {

        @Test
        @DisplayName("모든 대상 지역과 모든 프로바이더에서 데이터를 수집한다")
        void collectsFromAllAreasAndProviders() {
            // given
            LocalDate targetDate = LocalDate.of(2025, 1, 15);

            when(mockProvider1.execute(eq("서울"), any()))
                    .thenReturn(List.of(createSubscription("서울 아파트1")));
            when(mockProvider1.execute(eq("경기"), any()))
                    .thenReturn(List.of(createSubscription("경기 아파트1")));
            when(mockProvider2.execute(eq("서울"), any()))
                    .thenReturn(List.of(createSubscription("서울 아파트2")));
            when(mockProvider2.execute(eq("경기"), any()))
                    .thenReturn(List.of(createSubscription("경기 아파트2")));

            // when
            List<SubscriptionInfo> results = collector.collectFromAllAreas(targetDate);

            // then
            assertThat(results).hasSize(4);
            assertThat(results).extracting(SubscriptionInfo::getHouseName)
                    .containsExactlyInAnyOrder("서울 아파트1", "서울 아파트2", "경기 아파트1", "경기 아파트2");
        }

        @Test
        @DisplayName("프로바이더가 빈 결과를 반환해도 정상 처리한다")
        void handlesEmptyResultsFromProviders() {
            // given
            LocalDate targetDate = LocalDate.of(2025, 1, 15);

            when(mockProvider1.execute(any(), any())).thenReturn(List.of());
            when(mockProvider2.execute(any(), any())).thenReturn(List.of());

            // when
            List<SubscriptionInfo> results = collector.collectFromAllAreas(targetDate);

            // then
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("각 지역별로 프로바이더를 호출한다")
        void callsProvidersForEachArea() {
            // given
            LocalDate targetDate = LocalDate.of(2025, 1, 15);
            when(mockProvider1.execute(any(), any())).thenReturn(List.of());
            when(mockProvider2.execute(any(), any())).thenReturn(List.of());

            // when
            collector.collectFromAllAreas(targetDate);

            // then
            verify(mockProvider1).execute("서울", targetDate);
            verify(mockProvider1).execute("경기", targetDate);
            verify(mockProvider2).execute("서울", targetDate);
            verify(mockProvider2).execute("경기", targetDate);
        }
    }

    @Nested
    @DisplayName("collectFromArea() - 특정 지역에서 수집")
    class CollectFromArea {

        @Test
        @DisplayName("특정 지역의 모든 프로바이더에서 데이터를 수집한다")
        void collectsFromAllProvidersForArea() {
            // given
            LocalDate targetDate = LocalDate.of(2025, 1, 15);

            when(mockProvider1.execute("서울", targetDate))
                    .thenReturn(List.of(createSubscription("아파트1")));
            when(mockProvider2.execute("서울", targetDate))
                    .thenReturn(List.of(createSubscription("아파트2")));

            // when
            List<SubscriptionInfo> results = collector.collectFromArea("서울", targetDate);

            // then
            assertThat(results).hasSize(2);
        }
    }


    private SubscriptionInfo createSubscription(String houseName) {
        return ApplyHomeSubscriptionInfo.builder()
                .houseName(houseName)
                .area("서울")
                .build();
    }
}
