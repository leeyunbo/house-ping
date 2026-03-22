package com.yunbok.houseping.scheduler;

import com.yunbok.houseping.core.service.competition.CompetitionRateCollectorService;
import com.yunbok.houseping.support.dto.SchedulerResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("CompetitionRateScheduler - 경쟁률 수집 스케줄러")
@ExtendWith(MockitoExtension.class)
class CompetitionRateSchedulerTest {

    @Mock
    private CompetitionRateCollectorService collectorUseCase;

    private CompetitionRateScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new CompetitionRateScheduler(collectorUseCase);
    }

    @Nested
    @DisplayName("collectCompetitionRates() - 경쟁률 수집")
    class CollectCompetitionRates {

        @Test
        @DisplayName("수집 결과를 SchedulerResult로 반환한다")
        void returnsSchedulerResult() {
            // given
            when(collectorUseCase.collect()).thenReturn(10);

            // when
            SchedulerResult result = scheduler.collectCompetitionRates();

            // then
            assertThat(result.successCount()).isEqualTo(10);
            assertThat(result.hasFailed()).isFalse();
            verify(collectorUseCase).collect();
        }
    }
}
