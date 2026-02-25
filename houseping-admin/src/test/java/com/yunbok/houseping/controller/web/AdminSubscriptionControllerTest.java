package com.yunbok.houseping.controller.web;

import com.yunbok.houseping.support.dto.CalendarEventDto;
import com.yunbok.houseping.service.dto.AdminSubscriptionDto;
import com.yunbok.houseping.service.AdminSubscriptionQueryService;
import com.yunbok.houseping.core.service.subscription.SubscriptionManagementService;
import com.yunbok.houseping.core.service.subscription.SubscriptionAnalysisService;
import com.yunbok.houseping.service.dto.AdminSubscriptionSearchCriteria;
import com.yunbok.houseping.support.dto.SyncResult;
import com.yunbok.houseping.entity.SubscriptionPriceEntity;
import com.yunbok.houseping.repository.SubscriptionPriceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("AdminSubscriptionController - 관리자 UI 컨트롤러")
@ExtendWith(MockitoExtension.class)
class AdminSubscriptionControllerTest {

    @Mock
    private AdminSubscriptionQueryService queryService;

    @Mock
    private SubscriptionManagementService managementUseCase;

    @Mock
    private SubscriptionPriceRepository priceRepository;

    @Mock
    private SubscriptionAnalysisService analysisService;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    private AdminSubscriptionController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminSubscriptionController(queryService, managementUseCase, priceRepository, analysisService);
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

    @Nested
    @DisplayName("calendar() - 캘린더 페이지")
    class CalendarPage {

        @Test
        @DisplayName("템플릿 경로 admin/subscriptions/calendar를 반환한다")
        void returnsCorrectViewName() {
            // given
            Model model = mock(Model.class);

            // when
            String viewName = controller.calendar(model);

            // then
            assertThat(viewName).isEqualTo("admin/subscriptions/calendar");
        }
    }

    @Nested
    @DisplayName("calendarEvents() - 캘린더 이벤트 조회")
    class CalendarEvents {

        @Test
        @DisplayName("이벤트 목록을 반환한다")
        void returnsEvents() {
            // given
            LocalDate start = LocalDate.of(2025, 1, 1);
            LocalDate end = LocalDate.of(2025, 1, 31);
            CalendarEventDto.ExtendedProps extendedProps = new CalendarEventDto.ExtendedProps(
                    "테스트 아파트", "서울", "ApplyHome", "APT",
                    start, start, end, end.plusMonths(1), 100,
                    "http://detail.url", "receipt", false, false,
                    "서울특별시 강남구 테헤란로 123", "06134"
            );
            List<CalendarEventDto> events = List.of(
                    new CalendarEventDto(1L, "테스트 아파트", start, end, "#3498db", "#ffffff", extendedProps)
            );
            when(queryService.getCalendarEvents(start, end)).thenReturn(events);

            // when
            ResponseEntity<List<CalendarEventDto>> response = controller.calendarEvents(start, end);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getById() - ID로 조회")
    class GetById {

        @Test
        @DisplayName("존재하는 ID면 데이터를 반환한다")
        void returnsDataWhenFound() {
            // given
            AdminSubscriptionDto dto = createDto();
            when(queryService.findById(1L)).thenReturn(Optional.of(dto));

            // when
            ResponseEntity<AdminSubscriptionDto> response = controller.getById(1L);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
        }

        @Test
        @DisplayName("존재하지 않는 ID면 404를 반환한다")
        void returnsNotFoundWhenMissing() {
            // given
            when(queryService.findById(999L)).thenReturn(Optional.empty());

            // when
            ResponseEntity<AdminSubscriptionDto> response = controller.getById(999L);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("sync() - 동기화")
    class Sync {

        @Test
        @DisplayName("동기화 성공 시 리다이렉트한다")
        void redirectsAfterSync() {
            // given
            when(managementUseCase.sync()).thenReturn(new SyncResult(5, 3, 0));

            // when
            String result = controller.sync(redirectAttributes);

            // then
            assertThat(result).isEqualTo("redirect:/admin/subscriptions");
        }

        @Test
        @DisplayName("동기화 성공 시 메시지를 추가한다")
        void addsSuccessMessage() {
            // given
            when(managementUseCase.sync()).thenReturn(new SyncResult(5, 3, 0));

            // when
            controller.sync(redirectAttributes);

            // then
            verify(redirectAttributes).addFlashAttribute(eq("message"), contains("신규 5건"));
        }

        @Test
        @DisplayName("동기화 실패 시 에러 메시지를 추가한다")
        void addsErrorMessageOnFailure() {
            // given
            when(managementUseCase.sync()).thenThrow(new RuntimeException("API 오류"));

            // when
            controller.sync(redirectAttributes);

            // then
            verify(redirectAttributes).addFlashAttribute(eq("error"), contains("동기화 실패"));
        }
    }

    @Nested
    @DisplayName("toggleNotification() - 알림 토글")
    class ToggleNotification {

        @Test
        @DisplayName("알림 설정 시 enabled true를 반환한다")
        void returnsEnabledTrue() {
            // given
            when(queryService.toggleNotification(1L)).thenReturn(true);

            // when
            ResponseEntity<Map<String, Object>> response = controller.toggleNotification(1L);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).containsEntry("enabled", true);
            assertThat(response.getBody()).containsEntry("success", true);
        }

        @Test
        @DisplayName("알림 해제 시 enabled false를 반환한다")
        void returnsEnabledFalse() {
            // given
            when(queryService.toggleNotification(1L)).thenReturn(false);

            // when
            ResponseEntity<Map<String, Object>> response = controller.toggleNotification(1L);

            // then
            assertThat(response.getBody()).containsEntry("enabled", false);
        }
    }

    @Nested
    @DisplayName("removeNotification() - 알림 제거")
    class RemoveNotification {

        @Test
        @DisplayName("성공 응답을 반환한다")
        void returnsSuccessResponse() {
            // when
            ResponseEntity<Map<String, Object>> response = controller.removeNotification(1L);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).containsEntry("success", true);
        }

        @Test
        @DisplayName("queryService.removeNotification을 호출한다")
        void callsRemoveNotification() {
            // when
            controller.removeNotification(1L);

            // then
            verify(queryService).removeNotification(1L);
        }
    }

    @Nested
    @DisplayName("getPrices() - 분양가 조회")
    class GetPrices {

        @Test
        @DisplayName("청약이 존재하고 분양가가 있으면 목록을 반환한다")
        void returnsPricesWhenExist() {
            // given
            AdminSubscriptionDto dto = createDtoWithHouseManageNo("2024000001");
            when(queryService.findById(1L)).thenReturn(Optional.of(dto));

            SubscriptionPriceEntity priceEntity = SubscriptionPriceEntity.builder()
                    .houseManageNo("2024000001")
                    .pblancNo("2024000001")
                    .modelNo("001")
                    .houseType("59A")
                    .supplyArea(BigDecimal.valueOf(84.12))
                    .supplyCount(100)
                    .specialSupplyCount(20)
                    .topAmount(52300L)
                    .pricePerPyeong(2100L)
                    .build();
            when(priceRepository.findByHouseManageNo("2024000001")).thenReturn(List.of(priceEntity));

            // when
            ResponseEntity<List<AdminSubscriptionController.PriceDto>> response = controller.getPrices(1L);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).houseType()).isEqualTo("59A");
            assertThat(response.getBody().get(0).topAmount()).isEqualTo(52300L);
        }

        @Test
        @DisplayName("청약이 존재하지만 분양가가 없으면 빈 목록을 반환한다")
        void returnsEmptyWhenNoPrices() {
            // given
            AdminSubscriptionDto dto = createDtoWithHouseManageNo("2024000001");
            when(queryService.findById(1L)).thenReturn(Optional.of(dto));
            when(priceRepository.findByHouseManageNo("2024000001")).thenReturn(List.of());

            // when
            ResponseEntity<List<AdminSubscriptionController.PriceDto>> response = controller.getPrices(1L);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }

        @Test
        @DisplayName("houseManageNo가 없으면 빈 목록을 반환한다")
        void returnsEmptyWhenNoHouseManageNo() {
            // given
            AdminSubscriptionDto dto = createDtoWithHouseManageNo(null);
            when(queryService.findById(1L)).thenReturn(Optional.of(dto));

            // when
            ResponseEntity<List<AdminSubscriptionController.PriceDto>> response = controller.getPrices(1L);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }

        @Test
        @DisplayName("청약이 존재하지 않으면 404를 반환한다")
        void returnsNotFoundWhenNoSubscription() {
            // given
            when(queryService.findById(999L)).thenReturn(Optional.empty());

            // when
            ResponseEntity<List<AdminSubscriptionController.PriceDto>> response = controller.getPrices(999L);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    private void mockQueryServiceDefaults() {
        when(queryService.search(any())).thenReturn(Page.empty());
        when(queryService.availableAreas()).thenReturn(List.of());
        when(queryService.availableHouseTypes()).thenReturn(List.of());
        when(queryService.availableSources()).thenReturn(List.of());
    }

    private AdminSubscriptionDto createDto() {
        return createDtoWithHouseManageNo(null);
    }

    private AdminSubscriptionDto createDtoWithHouseManageNo(String houseManageNo) {
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
                LocalDateTime.now(),
                "서울특별시 강남구 테헤란로 123",
                "06134",
                houseManageNo
        );
    }
}
