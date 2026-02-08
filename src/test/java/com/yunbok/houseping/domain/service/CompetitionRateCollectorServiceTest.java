package com.yunbok.houseping.domain.service;

import com.yunbok.houseping.core.service.competition.CompetitionRateCollectorService;

import com.yunbok.houseping.core.domain.CompetitionRate;
import com.yunbok.houseping.core.domain.SubscriptionConfig;
import com.yunbok.houseping.adapter.persistence.CompetitionRateDbAdapter;
import com.yunbok.houseping.core.port.CompetitionRateProvider;
import com.yunbok.houseping.adapter.persistence.SubscriptionPersistenceAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("CompetitionRateCollectorService - 경쟁률 수집 서비스")
@ExtendWith(MockitoExtension.class)
class CompetitionRateCollectorServiceTest {

    @Mock
    private CompetitionRateDbAdapter competitionRatePort;

    @Mock
    private SubscriptionPersistenceAdapter subscriptionPort;

    @Mock
    private CompetitionRateProvider competitionRateProvider;

    private SubscriptionConfig config;

    private CompetitionRateCollectorService service;

    @BeforeEach
    void setUp() {
        config = new SubscriptionConfig(List.of("서울", "경기"));
    }

    @Nested
    @DisplayName("collect() - 경쟁률 수집")
    class Collect {

        @Test
        @DisplayName("Provider가 없으면 0을 반환한다")
        void returnsZeroWhenNoProvider() {
            // given
            service = new CompetitionRateCollectorService(
                    competitionRatePort, subscriptionPort, Optional.empty(), config);

            // when
            int result = service.collect();

            // then
            assertThat(result).isEqualTo(0);
        }

        @Test
        @DisplayName("대상 지역에 청약 데이터가 없으면 0을 반환한다")
        void returnsZeroWhenNoSubscriptions() {
            // given
            service = new CompetitionRateCollectorService(
                    competitionRatePort, subscriptionPort, Optional.of(competitionRateProvider), config);
            when(subscriptionPort.findHouseManageNosByAreas(config.targetAreas())).thenReturn(Set.of());

            // when
            int result = service.collect();

            // then
            assertThat(result).isEqualTo(0);
            verify(competitionRateProvider, never()).fetchAll();
        }

        @Test
        @DisplayName("신규 데이터가 없으면 0을 반환한다")
        void returnsZeroWhenNoNewData() {
            // given
            service = new CompetitionRateCollectorService(
                    competitionRatePort, subscriptionPort, Optional.of(competitionRateProvider), config);

            Set<String> houseManageNos = Set.of("H001", "H002");
            when(subscriptionPort.findHouseManageNosByAreas(config.targetAreas())).thenReturn(houseManageNos);

            List<CompetitionRate> allRates = List.of(
                    createRate("H001", "P001"),
                    createRate("H003", "P003") // 대상 지역 아님
            );
            when(competitionRateProvider.fetchAll()).thenReturn(allRates);
            when(competitionRatePort.existsByHouseManageNoAndPblancNo("H001", "P001")).thenReturn(true); // 이미 존재

            // when
            int result = service.collect();

            // then
            assertThat(result).isEqualTo(0);
            verify(competitionRatePort, never()).saveAll(any());
        }

        @Test
        @DisplayName("신규 데이터만 필터링하여 저장한다")
        void savesOnlyNewData() {
            // given
            service = new CompetitionRateCollectorService(
                    competitionRatePort, subscriptionPort, Optional.of(competitionRateProvider), config);

            Set<String> houseManageNos = Set.of("H001", "H002", "H003");
            when(subscriptionPort.findHouseManageNosByAreas(config.targetAreas())).thenReturn(houseManageNos);

            List<CompetitionRate> allRates = List.of(
                    createRate("H001", "P001"),
                    createRate("H002", "P002"),
                    createRate("H003", "P003"),
                    createRate("H004", "P004") // 대상 지역 아님
            );
            when(competitionRateProvider.fetchAll()).thenReturn(allRates);
            when(competitionRatePort.existsByHouseManageNoAndPblancNo("H001", "P001")).thenReturn(true);  // 이미 존재
            when(competitionRatePort.existsByHouseManageNoAndPblancNo("H002", "P002")).thenReturn(false); // 신규
            when(competitionRatePort.existsByHouseManageNoAndPblancNo("H003", "P003")).thenReturn(false); // 신규

            // when
            int result = service.collect();

            // then
            assertThat(result).isEqualTo(2);
            verify(competitionRatePort).saveAll(argThat(list -> list.size() == 2));
        }

        @Test
        @DisplayName("API에서 전체 조회 후 대상 지역만 필터링한다")
        void filtersTargetAreasOnly() {
            // given
            service = new CompetitionRateCollectorService(
                    competitionRatePort, subscriptionPort, Optional.of(competitionRateProvider), config);

            Set<String> houseManageNos = Set.of("H001"); // 서울/경기 지역
            when(subscriptionPort.findHouseManageNosByAreas(config.targetAreas())).thenReturn(houseManageNos);

            List<CompetitionRate> allRates = List.of(
                    createRate("H001", "P001"), // 대상 지역
                    createRate("H002", "P002"), // 다른 지역
                    createRate("H003", "P003")  // 다른 지역
            );
            when(competitionRateProvider.fetchAll()).thenReturn(allRates);
            when(competitionRatePort.existsByHouseManageNoAndPblancNo(anyString(), anyString())).thenReturn(false);

            // when
            int result = service.collect();

            // then
            assertThat(result).isEqualTo(1);
            verify(competitionRatePort).saveAll(argThat(list ->
                list.size() == 1 && list.get(0).getHouseManageNo().equals("H001")));
        }
    }

    private CompetitionRate createRate(String houseManageNo, String pblancNo) {
        return CompetitionRate.builder()
                .houseManageNo(houseManageNo)
                .pblancNo(pblancNo)
                .houseType("084T")
                .supplyCount(100)
                .requestCount(500)
                .competitionRate(new BigDecimal("5.0"))
                .residenceArea("해당지역")
                .rank(1)
                .build();
    }
}
