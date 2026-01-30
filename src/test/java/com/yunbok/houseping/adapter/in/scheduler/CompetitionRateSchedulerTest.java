package com.yunbok.houseping.adapter.in.scheduler;

import com.yunbok.houseping.domain.service.CompetitionRateCollectorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@DisplayName("CompetitionRateScheduler - 경쟁률 수집 스케줄러")
@ExtendWith(MockitoExtension.class)
class CompetitionRateSchedulerTest {

    @Mock
    private CompetitionRateCollectorService collectorService;

    private CompetitionRateScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new CompetitionRateScheduler(collectorService);
    }

    @Nested
    @DisplayName("collectCompetitionRates() - 경쟁률 수집")
    class CollectCompetitionRates {

        @Test
        @DisplayName("수집 서비스를 호출한다")
        void callsCollectorService() {
            // given
            when(collectorService.collect()).thenReturn(10);

            // when
            scheduler.collectCompetitionRates();

            // then
            verify(collectorService).collect();
        }

        @Test
        @DisplayName("예외가 발생해도 스케줄러가 중단되지 않는다")
        void handlesException() {
            // given
            when(collectorService.collect()).thenThrow(new RuntimeException("수집 실패"));

            // when
            scheduler.collectCompetitionRates();

            // then - 예외 처리됨
            verify(collectorService).collect();
        }
    }
}
