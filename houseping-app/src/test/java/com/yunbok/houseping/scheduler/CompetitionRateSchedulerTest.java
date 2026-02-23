package com.yunbok.houseping.scheduler;

import com.yunbok.houseping.core.service.competition.CompetitionRateCollectorService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yunbok.houseping.infrastructure.api.SchedulerErrorSlackClient;
import com.yunbok.houseping.infrastructure.formatter.SlackMessageFormatter;

import static org.mockito.Mockito.*;

@DisplayName("CompetitionRateScheduler - 경쟁률 수집 스케줄러")
@ExtendWith(MockitoExtension.class)
class CompetitionRateSchedulerTest {

    @Mock
    private CompetitionRateCollectorService collectorUseCase;

    private CompetitionRateScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new CompetitionRateScheduler(collectorUseCase, new SchedulerErrorSlackClient("", new SlackMessageFormatter()));
    }

    @Nested
    @DisplayName("collectCompetitionRates() - 경쟁률 수집")
    class CollectCompetitionRates {

        @Test
        @DisplayName("수집 서비스를 호출한다")
        void callsCollectorService() {
            // given
            when(collectorUseCase.collect()).thenReturn(10);

            // when
            scheduler.collectCompetitionRates();

            // then
            verify(collectorUseCase).collect();
        }

        @Test
        @DisplayName("예외가 발생해도 스케줄러가 중단되지 않는다")
        void handlesException() {
            // given
            when(collectorUseCase.collect()).thenThrow(new RuntimeException("수집 실패"));

            // when
            scheduler.collectCompetitionRates();

            // then - 예외 처리됨
            verify(collectorUseCase).collect();
        }
    }
}
