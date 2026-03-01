package com.yunbok.houseping.controller.web;

import com.yunbok.houseping.service.AdminCompetitionRateService;
import com.yunbok.houseping.core.service.competition.CompetitionRateCollectorService;
import com.yunbok.houseping.service.dto.AdminCompetitionRateSearchCriteria;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("AdminCompetitionRateController - 경쟁률 관리 컨트롤러")
@ExtendWith(MockitoExtension.class)
class AdminCompetitionRateControllerTest {

    @Mock
    private AdminCompetitionRateService queryService;

    @Mock
    private CompetitionRateCollectorService collectorUseCase;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    private AdminCompetitionRateController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminCompetitionRateController(queryService, collectorUseCase);
    }

    @Nested
    @DisplayName("list() - 경쟁률 목록")
    class List {

        @Test
        @DisplayName("경쟁률 목록 뷰 이름을 반환한다")
        void returnsCorrectViewName() {
            // given
            mockQueryServiceDefaults();
            AdminCompetitionRateSearchCriteria criteria = new AdminCompetitionRateSearchCriteria(
                    null, null, null, null, null, null, null, null, 0, 20);

            // when
            String viewName = controller.list(criteria, model);

            // then
            assertThat(viewName).isEqualTo("admin/competition-rates/list");
        }

        @Test
        @DisplayName("검색 결과를 모델에 추가한다")
        void addsResultPageToModel() {
            // given
            mockQueryServiceDefaults();
            AdminCompetitionRateSearchCriteria criteria = new AdminCompetitionRateSearchCriteria(
                    null, null, null, null, null, null, null, null, 0, 20);

            // when
            controller.list(criteria, model);

            // then
            verify(model).addAttribute(eq("resultPage"), any());
        }

        @Test
        @DisplayName("검색 조건을 모델에 추가한다")
        void addsSearchCriteriaToModel() {
            // given
            mockQueryServiceDefaults();
            AdminCompetitionRateSearchCriteria criteria = new AdminCompetitionRateSearchCriteria(
                    "084T", null, "서울", null, null, null, null, null, 0, 20);

            // when
            controller.list(criteria, model);

            // then
            verify(model).addAttribute("search", criteria);
        }

        @Test
        @DisplayName("주택형 목록을 모델에 추가한다")
        void addsHouseTypesToModel() {
            // given
            java.util.List<String> houseTypes = java.util.List.of("084T", "059A");
            when(queryService.search(any())).thenReturn(Page.empty());
            when(queryService.availableHouseTypes()).thenReturn(houseTypes);
            when(queryService.availableAreas()).thenReturn(java.util.List.of());

            AdminCompetitionRateSearchCriteria criteria = new AdminCompetitionRateSearchCriteria(
                    null, null, null, null, null, null, null, null, 0, 20);

            // when
            controller.list(criteria, model);

            // then
            verify(model).addAttribute("houseTypes", houseTypes);
        }

        @Test
        @DisplayName("지역 목록을 모델에 추가한다")
        void addsAreasToModel() {
            // given
            java.util.List<String> areas = java.util.List.of("서울", "경기");
            when(queryService.search(any())).thenReturn(Page.empty());
            when(queryService.availableHouseTypes()).thenReturn(java.util.List.of());
            when(queryService.availableAreas()).thenReturn(areas);

            AdminCompetitionRateSearchCriteria criteria = new AdminCompetitionRateSearchCriteria(
                    null, null, null, null, null, null, null, null, 0, 20);

            // when
            controller.list(criteria, model);

            // then
            verify(model).addAttribute("areas", areas);
        }

        private void mockQueryServiceDefaults() {
            when(queryService.search(any())).thenReturn(Page.empty());
            when(queryService.availableHouseTypes()).thenReturn(java.util.List.of());
            when(queryService.availableAreas()).thenReturn(java.util.List.of());
        }
    }

    @Nested
    @DisplayName("collect() - 경쟁률 수집")
    class Collect {

        @Test
        @DisplayName("수집 성공 시 목록 페이지로 리다이렉트한다")
        void redirectsToListOnSuccess() {
            // given
            when(collectorUseCase.collect()).thenReturn(10);

            // when
            String result = controller.collect(redirectAttributes);

            // then
            assertThat(result).isEqualTo("redirect:/admin/competition-rates");
        }

        @Test
        @DisplayName("수집 성공 시 성공 메시지를 추가한다")
        void addsSuccessMessageOnSuccess() {
            // given
            when(collectorUseCase.collect()).thenReturn(10);

            // when
            controller.collect(redirectAttributes);

            // then
            verify(redirectAttributes).addFlashAttribute(eq("message"), contains("10건"));
        }

        @Test
        @DisplayName("수집 실패 시 에러 메시지를 추가한다")
        void addsErrorMessageOnFailure() {
            // given
            when(collectorUseCase.collect()).thenThrow(new RuntimeException("API 오류"));

            // when
            String result = controller.collect(redirectAttributes);

            // then
            assertThat(result).isEqualTo("redirect:/admin/competition-rates");
            verify(redirectAttributes).addFlashAttribute(eq("error"), contains("수집 실패"));
        }
    }

    @Nested
    @DisplayName("deleteAll() - 전체 삭제")
    class DeleteAll {

        @Test
        @DisplayName("삭제 성공 시 목록 페이지로 리다이렉트한다")
        void redirectsToListOnSuccess() {
            // given
            doNothing().when(queryService).deleteAll();

            // when
            String result = controller.deleteAll(redirectAttributes);

            // then
            assertThat(result).isEqualTo("redirect:/admin/competition-rates");
        }

        @Test
        @DisplayName("삭제 성공 시 성공 메시지를 추가한다")
        void addsSuccessMessageOnSuccess() {
            // given
            doNothing().when(queryService).deleteAll();

            // when
            controller.deleteAll(redirectAttributes);

            // then
            verify(redirectAttributes).addFlashAttribute(eq("message"), contains("삭제"));
        }

        @Test
        @DisplayName("삭제 실패 시 에러 메시지를 추가한다")
        void addsErrorMessageOnFailure() {
            // given
            doThrow(new RuntimeException("DB 오류")).when(queryService).deleteAll();

            // when
            String result = controller.deleteAll(redirectAttributes);

            // then
            assertThat(result).isEqualTo("redirect:/admin/competition-rates");
            verify(redirectAttributes).addFlashAttribute(eq("error"), contains("삭제 실패"));
        }
    }
}
