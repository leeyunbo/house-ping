package com.yunbok.houseping.adapter.in.web.home;

import com.yunbok.houseping.domain.model.Subscription;
import com.yunbok.houseping.domain.model.SubscriptionAnalysisResult;
import com.yunbok.houseping.domain.model.SubscriptionStatus;
import com.yunbok.houseping.domain.port.in.SubscriptionAnalysisUseCase;
import com.yunbok.houseping.domain.port.in.SubscriptionQueryUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("PublicHomeController - 공개 홈페이지 컨트롤러")
@ExtendWith(MockitoExtension.class)
class PublicHomeControllerTest {

    @Mock
    private SubscriptionQueryUseCase subscriptionQueryUseCase;

    @Mock
    private SubscriptionAnalysisUseCase subscriptionAnalysisUseCase;

    @Mock
    private Model model;

    private PublicHomeController controller;

    @BeforeEach
    void setUp() {
        controller = new PublicHomeController(subscriptionQueryUseCase, subscriptionAnalysisUseCase);
    }

    @Nested
    @DisplayName("index() - 랜딩 페이지")
    class Index {

        @Test
        @DisplayName("home/index 뷰 이름을 반환한다")
        void returnsCorrectViewName() {
            // given
            when(subscriptionQueryUseCase.findActiveAndUpcomingSubscriptions(null))
                    .thenReturn(Collections.emptyList());
            when(subscriptionQueryUseCase.filterActiveSubscriptions(any()))
                    .thenReturn(Collections.emptyList());
            when(subscriptionQueryUseCase.filterUpcomingSubscriptions(any()))
                    .thenReturn(Collections.emptyList());

            // when
            String viewName = controller.index(null, null, model);

            // then
            assertThat(viewName).isEqualTo("home/index");
        }

        @Test
        @DisplayName("접수중 청약과 예정 청약을 분류하여 모델에 추가한다")
        void separatesActiveAndUpcomingSubscriptions() {
            // given
            LocalDate today = LocalDate.now();
            List<Subscription> subscriptions = List.of(
                    createSubscription(1L, "접수중 아파트", today.minusDays(1), today.plusDays(5)),
                    createSubscription(2L, "예정 아파트", today.plusDays(3), today.plusDays(10))
            );
            when(subscriptionQueryUseCase.findActiveAndUpcomingSubscriptions(null))
                    .thenReturn(subscriptions);
            when(subscriptionQueryUseCase.filterActiveSubscriptions(subscriptions))
                    .thenReturn(List.of(subscriptions.get(0)));
            when(subscriptionQueryUseCase.filterUpcomingSubscriptions(subscriptions))
                    .thenReturn(List.of(subscriptions.get(1)));

            // when
            controller.index(null, null, model);

            // then
            verify(model).addAttribute(eq("activeSubscriptions"), any());
            verify(model).addAttribute(eq("upcomingSubscriptions"), any());
        }

        @Test
        @DisplayName("지역 필터가 있으면 해당 지역으로 조회한다")
        void filtersByArea() {
            // given
            when(subscriptionQueryUseCase.findActiveAndUpcomingSubscriptions("서울"))
                    .thenReturn(Collections.emptyList());
            when(subscriptionQueryUseCase.filterActiveSubscriptions(any()))
                    .thenReturn(Collections.emptyList());
            when(subscriptionQueryUseCase.filterUpcomingSubscriptions(any()))
                    .thenReturn(Collections.emptyList());

            // when
            controller.index("서울", null, model);

            // then
            verify(subscriptionQueryUseCase).findActiveAndUpcomingSubscriptions("서울");
        }

        @Test
        @DisplayName("지역 목록을 모델에 추가한다")
        void addsAreasToModel() {
            // given
            when(subscriptionQueryUseCase.findActiveAndUpcomingSubscriptions(null))
                    .thenReturn(Collections.emptyList());
            when(subscriptionQueryUseCase.filterActiveSubscriptions(any()))
                    .thenReturn(Collections.emptyList());
            when(subscriptionQueryUseCase.filterUpcomingSubscriptions(any()))
                    .thenReturn(Collections.emptyList());

            // when
            controller.index(null, null, model);

            // then
            verify(model).addAttribute(eq("areas"), any(List.class));
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
        @DisplayName("청약을 찾을 수 없으면 홈으로 리다이렉트한다")
        void redirectsToHomeWhenNotFound() {
            // given
            when(subscriptionAnalysisUseCase.analyze(999L))
                    .thenThrow(new IllegalArgumentException("청약 정보를 찾을 수 없습니다"));

            // when
            String viewName = controller.analysis(999L, model);

            // then
            assertThat(viewName).isEqualTo("redirect:/home");
        }

        @Test
        @DisplayName("상태 라벨을 모델에 추가한다")
        void addsStatusLabelToModel() {
            // given
            Subscription subscription = createSubscription(1L, "테스트 아파트", LocalDate.now().minusDays(1), LocalDate.now().plusDays(5));
            SubscriptionAnalysisResult analysis = SubscriptionAnalysisResult.builder()
                    .subscription(subscription)
                    .prices(List.of())
                    .build();
            when(subscriptionAnalysisUseCase.analyze(1L)).thenReturn(analysis);

            // when
            controller.analysis(1L, model);

            // then
            verify(model).addAttribute(eq("statusLabel"), any(String.class));
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
