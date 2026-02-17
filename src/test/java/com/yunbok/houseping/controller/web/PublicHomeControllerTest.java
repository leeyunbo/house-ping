package com.yunbok.houseping.controller.web;

import com.yunbok.houseping.core.service.subscription.SubscriptionSearchService;
import com.yunbok.houseping.core.service.subscription.SubscriptionAnalysisService;
import com.yunbok.houseping.core.service.calendar.PublicCalendarService;
import com.yunbok.houseping.core.domain.Subscription;
import com.yunbok.houseping.support.dto.HomePageResult;
import com.yunbok.houseping.support.dto.SubscriptionAnalysisResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@DisplayName("PublicHomeController - 공개 홈페이지 컨트롤러")
@ExtendWith(MockitoExtension.class)
class PublicHomeControllerTest {

    @Mock
    private SubscriptionSearchService subscriptionSearchService;

    @Mock
    private SubscriptionAnalysisService subscriptionAnalysisUseCase;

    @Mock
    private PublicCalendarService publicCalendarService;

    @Mock
    private Model model;

    private PublicHomeController controller;

    @BeforeEach
    void setUp() {
        controller = new PublicHomeController(subscriptionSearchService, subscriptionAnalysisUseCase, publicCalendarService);
    }

    @Nested
    @DisplayName("index() - 랜딩 페이지")
    class Index {

        @Test
        @DisplayName("home/index 뷰 이름을 반환한다")
        void returnsCorrectViewName() {
            // given
            when(subscriptionSearchService.getHomeData(null, null))
                    .thenReturn(HomePageResult.builder()
                            .activeSubscriptions(List.of())
                            .upcomingSubscriptions(List.of())
                            .areas(List.of("서울", "경기"))
                            .build());

            // when
            String viewName = controller.index(null, null, model);

            // then
            assertThat(viewName).isEqualTo("home/index");
        }

        @Test
        @DisplayName("서비스 결과를 home으로 모델에 추가한다")
        void addsHomeDataToModel() {
            // given
            HomePageResult homeData = HomePageResult.builder()
                    .activeSubscriptions(List.of())
                    .upcomingSubscriptions(List.of())
                    .areas(List.of("서울", "경기"))
                    .build();
            when(subscriptionSearchService.getHomeData(null, null)).thenReturn(homeData);

            // when
            controller.index(null, null, model);

            // then
            verify(model).addAttribute("home", homeData);
        }

        @Test
        @DisplayName("지역 필터가 있으면 해당 지역으로 조회한다")
        void filtersByArea() {
            // given
            when(subscriptionSearchService.getHomeData("서울", null))
                    .thenReturn(HomePageResult.builder()
                            .activeSubscriptions(List.of())
                            .upcomingSubscriptions(List.of())
                            .areas(List.of("서울", "경기"))
                            .selectedArea("서울")
                            .build());

            // when
            controller.index("서울", null, model);

            // then
            verify(subscriptionSearchService).getHomeData("서울", null);
        }
    }

    @Nested
    @DisplayName("analysis() - 청약 분석 페이지")
    class Analysis {

        @Test
        @DisplayName("home/analysis 뷰 이름을 반환한다")
        void returnsCorrectViewName() {
            // given
            Subscription subscription = createSubscription(1L, "테스트 아파트", LocalDate.now(), LocalDate.now().plusDays(5));
            SubscriptionAnalysisResult analysis = SubscriptionAnalysisResult.builder()
                    .subscription(subscription)
                    .prices(List.of())
                    .build();
            when(subscriptionAnalysisUseCase.analyze(1L)).thenReturn(analysis);

            // when
            String viewName = controller.analysis(1L, model);

            // then
            assertThat(viewName).isEqualTo("home/analysis");
        }

        @Test
        @DisplayName("분석 데이터를 모델에 추가한다")
        void addsAnalysisToModel() {
            // given
            Subscription subscription = createSubscription(1L, "테스트 아파트", LocalDate.now(), LocalDate.now().plusDays(5));
            SubscriptionAnalysisResult analysis = SubscriptionAnalysisResult.builder()
                    .subscription(subscription)
                    .prices(List.of())
                    .build();
            when(subscriptionAnalysisUseCase.analyze(1L)).thenReturn(analysis);

            // when
            controller.analysis(1L, model);

            // then
            verify(model).addAttribute("analysis", analysis);
            verify(model).addAttribute("subscription", subscription);
        }

        @Test
        @DisplayName("청약을 찾을 수 없으면 예외가 발생한다")
        void throwsExceptionWhenNotFound() {
            // given
            when(subscriptionAnalysisUseCase.analyze(999L))
                    .thenThrow(new IllegalArgumentException("청약 정보를 찾을 수 없습니다"));

            // when & then
            assertThatThrownBy(() -> controller.analysis(999L, model))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    private Subscription createSubscription(Long id, String name, LocalDate startDate, LocalDate endDate) {
        return Subscription.builder()
                .id(id)
                .source("ApplyHome")
                .houseName(name)
                .area("서울")
                .receiptStartDate(startDate)
                .receiptEndDate(endDate)
                .build();
    }
}
