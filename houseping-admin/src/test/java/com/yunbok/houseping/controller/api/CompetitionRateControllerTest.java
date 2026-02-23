package com.yunbok.houseping.controller.api;

import com.yunbok.houseping.core.service.competition.CompetitionRateCollectorService;
import com.yunbok.houseping.support.dto.ApiResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("CompetitionRateController - 경쟁률 수집 컨트롤러")
@ExtendWith(MockitoExtension.class)
class CompetitionRateControllerTest {

    @Mock
    private CompetitionRateCollectorService collectorUseCase;

    private CompetitionRateController controller;

    @BeforeEach
    void setUp() {
        controller = new CompetitionRateController(collectorUseCase);
    }

    @Test
    @DisplayName("collect()는 경쟁률 수집을 실행한다")
    void collectExecutesCollector() {
        // given
        when(collectorUseCase.collect()).thenReturn(10);

        // when
        controller.collect();

        // then
        verify(collectorUseCase).collect();
    }

    @Test
    @DisplayName("collect()는 수집된 개수를 반환한다")
    void collectReturnsCollectedCount() {
        // given
        when(collectorUseCase.collect()).thenReturn(25);

        // when
        ApiResponse<Map<String, Integer>> response = controller.collect();

        // then
        assertThat(response.data()).containsEntry("collectedCount", 25);
    }

    @Test
    @DisplayName("collect()는 성공 메시지를 포함한다")
    void collectIncludesSuccessMessage() {
        // given
        when(collectorUseCase.collect()).thenReturn(0);

        // when
        ApiResponse<Map<String, Integer>> response = controller.collect();

        // then
        assertThat(response.message()).contains("Competition rate collection completed");
    }

    @Test
    @DisplayName("수집 결과가 0개여도 정상 응답한다")
    void collectHandlesZeroResults() {
        // given
        when(collectorUseCase.collect()).thenReturn(0);

        // when
        ApiResponse<Map<String, Integer>> response = controller.collect();

        // then
        assertThat(response.data()).containsEntry("collectedCount", 0);
        assertThat(response.success()).isTrue();
    }
}
