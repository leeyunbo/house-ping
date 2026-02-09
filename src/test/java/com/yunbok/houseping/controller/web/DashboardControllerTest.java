package com.yunbok.houseping.controller.web;

import com.yunbok.houseping.core.service.admin.DashboardQueryService;
import com.yunbok.houseping.controller.web.dto.DashboardStatisticsDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("DashboardController - 대시보드 컨트롤러")
@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private DashboardQueryService dashboardQueryService;

    @Mock
    private Model model;

    private DashboardController controller;

    @BeforeEach
    void setUp() {
        controller = new DashboardController(dashboardQueryService);
    }

    @Nested
    @DisplayName("dashboard() - 대시보드 페이지")
    class Dashboard {

        @Test
        @DisplayName("대시보드 뷰 이름을 반환한다")
        void returnsCorrectViewName() {
            // given
            DashboardStatisticsDto stats = createEmptyStats();
            when(dashboardQueryService.getStatistics()).thenReturn(stats);

            // when
            String viewName = controller.dashboard(model);

            // then
            assertThat(viewName).isEqualTo("admin/dashboard");
        }

        @Test
        @DisplayName("통계 데이터를 모델에 추가한다")
        void addsStatsToModel() {
            // given
            DashboardStatisticsDto stats = createEmptyStats();
            when(dashboardQueryService.getStatistics()).thenReturn(stats);

            // when
            controller.dashboard(model);

            // then
            verify(model).addAttribute("stats", stats);
        }

        @Test
        @DisplayName("DashboardQueryService의 getStatistics를 호출한다")
        void callsQueryService() {
            // given
            DashboardStatisticsDto stats = createEmptyStats();
            when(dashboardQueryService.getStatistics()).thenReturn(stats);

            // when
            controller.dashboard(model);

            // then
            verify(dashboardQueryService).getStatistics();
        }
    }

    private DashboardStatisticsDto createEmptyStats() {
        return new DashboardStatisticsDto(
                new DashboardStatisticsDto.Summary(0, null, null, null, 0, 0),
                new DashboardStatisticsDto.AreaYearlyTrend(List.of(), List.of()),
                List.of(),
                List.of(),
                new DashboardStatisticsDto.RateDistribution(0, 0, 0, 0, 0)
        );
    }
}
