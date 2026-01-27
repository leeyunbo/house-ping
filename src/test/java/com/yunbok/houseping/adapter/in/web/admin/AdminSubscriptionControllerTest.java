package com.yunbok.houseping.adapter.in.web.admin;

import com.yunbok.houseping.domain.port.in.SubscriptionManagementUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("AdminSubscriptionController - 관리자 UI 컨트롤러")
@ExtendWith(MockitoExtension.class)
class AdminSubscriptionControllerTest {

    @Mock
    private AdminSubscriptionQueryService queryService;

    @Mock
    private SubscriptionManagementUseCase managementUseCase;

    @Mock
    private Model model;

    private AdminSubscriptionController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminSubscriptionController(queryService, managementUseCase);
    }

    @Nested
    @DisplayName("list() - 청약 목록 페이지")
    class ListPage {

        @Test
        @DisplayName("템플릿 경로 admin/subscriptions/list를 반환한다")
        void returnsCorrectViewName() {
            // given
            mockQueryServiceDefaults();
            AdminSubscriptionSearchCriteria criteria = new AdminSubscriptionSearchCriteria(
                    null, null, null, null, null, null, 0, 20
            );

            // when
            String viewName = controller.list(criteria, model);

            // then
            assertThat(viewName).isEqualTo("admin/subscriptions/list");
        }

        @Test
        @DisplayName("검색 조건을 QueryService에 전달한다")
        void passesSearchCriteriaToService() {
            // given
            mockQueryServiceDefaults();
            AdminSubscriptionSearchCriteria criteria = new AdminSubscriptionSearchCriteria(
                    "힐스테이트", "서울", null, "ApplyHome",
                    LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31),
                    2, 50
            );

            // when
            controller.list(criteria, model);

            // then
            verify(queryService).search(criteria);
        }

        @Test
        @DisplayName("결과 페이지를 모델에 추가한다")
        void addsResultPageToModel() {
            // given
            Page<AdminSubscriptionDto> resultPage = new PageImpl<>(List.of(createDto()));
            when(queryService.search(any())).thenReturn(resultPage);
            when(queryService.availableAreas()).thenReturn(List.of());
            when(queryService.availableHouseTypes()).thenReturn(List.of());
            when(queryService.availableSources()).thenReturn(List.of());

            AdminSubscriptionSearchCriteria criteria = new AdminSubscriptionSearchCriteria(
                    null, null, null, null, null, null, 0, 20
            );

            // when
            controller.list(criteria, model);

            // then
            verify(model).addAttribute("resultPage", resultPage);
        }

        @Test
        @DisplayName("검색 조건을 모델에 추가한다")
        void addsSearchCriteriaToModel() {
            // given
            mockQueryServiceDefaults();
            AdminSubscriptionSearchCriteria criteria = new AdminSubscriptionSearchCriteria(
                    "test", "서울", null, "LH", null, null, 0, 20
            );

            // when
            controller.list(criteria, model);

            // then
            verify(model).addAttribute("search", criteria);
        }

        @Test
        @DisplayName("사용 가능한 지역 목록을 모델에 추가한다")
        void addsAvailableAreasToModel() {
            // given
            List<String> areas = List.of("서울", "경기", "인천");
            when(queryService.search(any())).thenReturn(Page.empty());
            when(queryService.availableAreas()).thenReturn(areas);
            when(queryService.availableHouseTypes()).thenReturn(List.of());
            when(queryService.availableSources()).thenReturn(List.of());

            AdminSubscriptionSearchCriteria criteria = new AdminSubscriptionSearchCriteria(
                    null, null, null, null, null, null, 0, 20
            );

            // when
            controller.list(criteria, model);

            // then
            verify(model).addAttribute("areas", areas);
        }

        @Test
        @DisplayName("사용 가능한 소스 목록을 모델에 추가한다")
        void addsAvailableSourcesToModel() {
            // given
            List<String> sources = List.of("ApplyHome", "LH");
            when(queryService.search(any())).thenReturn(Page.empty());
            when(queryService.availableAreas()).thenReturn(List.of());
            when(queryService.availableHouseTypes()).thenReturn(List.of());
            when(queryService.availableSources()).thenReturn(sources);

            AdminSubscriptionSearchCriteria criteria = new AdminSubscriptionSearchCriteria(
                    null, null, null, null, null, null, 0, 20
            );

            // when
            controller.list(criteria, model);

            // then
            verify(model).addAttribute("sources", sources);
        }
    }

    private void mockQueryServiceDefaults() {
        when(queryService.search(any())).thenReturn(Page.empty());
        when(queryService.availableAreas()).thenReturn(List.of());
        when(queryService.availableHouseTypes()).thenReturn(List.of());
        when(queryService.availableSources()).thenReturn(List.of());
    }

    private AdminSubscriptionDto createDto() {
        return new AdminSubscriptionDto(
                1L,
                "ApplyHome",
                "테스트 아파트",
                "APT",
                "서울",
                LocalDate.now(),
                LocalDate.now(),
                LocalDate.now().plusDays(7),
                LocalDate.now().plusMonths(1),
                "http://detail.url",
                "http://homepage.url",
                "02-1234-5678",
                100,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
