package com.yunbok.houseping.core.service.blog;

import com.yunbok.houseping.core.domain.Subscription;
import com.yunbok.houseping.core.domain.SubscriptionPrice;
import com.yunbok.houseping.core.service.subscription.PriceBadgeCalculator;
import com.yunbok.houseping.core.service.subscription.SubscriptionAnalysisService;
import com.yunbok.houseping.core.service.subscription.SubscriptionSearchService;
import com.yunbok.houseping.support.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@DisplayName("WeeklyBlogContentService - 주간 블로그 콘텐츠 서비스")
@ExtendWith(MockitoExtension.class)
class WeeklyBlogContentServiceTest {

    @Mock
    private SubscriptionSearchService searchService;

    @Mock
    private SubscriptionAnalysisService analysisService;

    @Mock
    private PriceBadgeCalculator priceBadgeCalculator;

    @Mock
    private BlogCardImageGenerator cardImageGenerator;

    @Mock
    private BlogNarrativeBuilder narrativeBuilder;

    private WeeklyBlogContentService service;

    @BeforeEach
    void setUp() {
        service = new WeeklyBlogContentService(searchService, analysisService, priceBadgeCalculator, cardImageGenerator, narrativeBuilder);
    }

    @Nested
    @DisplayName("generateWeeklyContent() - 주간 콘텐츠 생성")
    class GenerateWeeklyContent {

        @Test
        @DisplayName("정상적으로 주간 블로그 콘텐츠를 생성한다")
        void generatesContentSuccessfully() {
            // given
            Subscription sub = createSubscription(1L, "테스트아파트", "서울",
                    LocalDate.now().minusDays(1), LocalDate.now().plusDays(5));
            SubscriptionCardView card = SubscriptionCardView.builder()
                    .subscription(sub).priceBadge(PriceBadge.CHEAP).build();

            SubscriptionAnalysisResult analysis = SubscriptionAnalysisResult.builder()
                    .subscription(sub)
                    .prices(List.of())
                    .recentTransactions(List.of())
                    .houseTypeComparisons(List.of())
                    .competitionRates(List.of())
                    .build();

            when(searchService.getAllActiveAndUpcoming()).thenReturn(List.of(card));
            when(analysisService.analyze(1L)).thenReturn(analysis);
            when(priceBadgeCalculator.computePriceBadge(any())).thenReturn(PriceBadge.CHEAP);
            when(cardImageGenerator.generateCardImage(any(), any())).thenReturn(new byte[]{1, 2, 3});
            when(narrativeBuilder.build(any(), eq(1))).thenReturn("테스트 내러티브");

            // when
            BlogContentResult result = service.generateWeeklyContent(1);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).contains("TOP 1");
            assertThat(result.getBlogText()).contains("하우스핑");
            assertThat(result.getEntries()).hasSize(1);
            assertThat(result.getEntries().get(0).getHouseName()).isEqualTo("테스트아파트");
        }

        @Test
        @DisplayName("분석 실패한 청약은 건너뛴다")
        void skipsFailedAnalysis() {
            // given
            Subscription sub1 = createSubscription(1L, "실패아파트", "서울",
                    LocalDate.now().minusDays(1), LocalDate.now().plusDays(5));
            Subscription sub2 = createSubscription(2L, "성공아파트", "경기",
                    LocalDate.now().minusDays(1), LocalDate.now().plusDays(5));
            SubscriptionCardView card1 = SubscriptionCardView.builder()
                    .subscription(sub1).priceBadge(PriceBadge.UNKNOWN).build();
            SubscriptionCardView card2 = SubscriptionCardView.builder()
                    .subscription(sub2).priceBadge(PriceBadge.CHEAP).build();

            SubscriptionAnalysisResult analysis2 = SubscriptionAnalysisResult.builder()
                    .subscription(sub2).prices(List.of()).recentTransactions(List.of())
                    .houseTypeComparisons(List.of()).competitionRates(List.of()).build();

            when(searchService.getAllActiveAndUpcoming()).thenReturn(List.of(card1, card2));
            when(analysisService.analyze(1L)).thenThrow(new RuntimeException("분석 실패"));
            when(analysisService.analyze(2L)).thenReturn(analysis2);
            when(priceBadgeCalculator.computePriceBadge(sub2)).thenReturn(PriceBadge.CHEAP);
            when(cardImageGenerator.generateCardImage(any(), any())).thenReturn(new byte[]{1});
            when(narrativeBuilder.build(any(), eq(1))).thenReturn("내러티브");

            // when
            BlogContentResult result = service.generateWeeklyContent(1);

            // then
            assertThat(result.getEntries()).hasSize(1);
            assertThat(result.getEntries().get(0).getHouseName()).isEqualTo("성공아파트");
        }

        @Test
        @DisplayName("활성 청약이 없으면 빈 entries를 반환한다")
        void returnsEmptyEntriesWhenNoActiveSubscriptions() {
            // given
            when(searchService.getAllActiveAndUpcoming()).thenReturn(List.of());

            // when
            BlogContentResult result = service.generateWeeklyContent(3);

            // then
            assertThat(result.getEntries()).isEmpty();
        }
    }

    @Nested
    @DisplayName("selectTopEntries() - TOP N 선정")
    class SelectTopEntries {

        @Test
        @DisplayName("스코어 기반으로 상위 N개를 선정한다")
        void selectsTopByScore() {
            // given
            Subscription cheap = createSubscription(1L, "저렴아파트", "서울",
                    LocalDate.now().minusDays(1), LocalDate.now().plusDays(5));
            cheap = Subscription.builder()
                    .id(1L).houseName("저렴아파트").area("서울").source("ApplyHome")
                    .receiptStartDate(LocalDate.now().minusDays(1)).receiptEndDate(LocalDate.now().plusDays(5))
                    .totalSupplyCount(1000).build();
            Subscription normal = createSubscription(2L, "일반아파트", "경기",
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(10));

            SubscriptionCardView card1 = SubscriptionCardView.builder()
                    .subscription(cheap).priceBadge(PriceBadge.CHEAP).build();
            SubscriptionCardView card2 = SubscriptionCardView.builder()
                    .subscription(normal).priceBadge(PriceBadge.UNKNOWN).build();

            SubscriptionAnalysisResult analysis1 = SubscriptionAnalysisResult.builder()
                    .subscription(cheap).prices(List.of()).recentTransactions(List.of())
                    .houseTypeComparisons(List.of()).competitionRates(List.of()).build();
            SubscriptionAnalysisResult analysis2 = SubscriptionAnalysisResult.builder()
                    .subscription(normal).prices(List.of()).recentTransactions(List.of())
                    .houseTypeComparisons(List.of()).competitionRates(List.of()).build();

            when(searchService.getAllActiveAndUpcoming()).thenReturn(List.of(card1, card2));
            when(analysisService.analyze(1L)).thenReturn(analysis1);
            when(analysisService.analyze(2L)).thenReturn(analysis2);
            when(priceBadgeCalculator.computePriceBadge(cheap)).thenReturn(PriceBadge.CHEAP);
            when(priceBadgeCalculator.computePriceBadge(normal)).thenReturn(PriceBadge.UNKNOWN);

            // when
            List<WeeklyBlogContentService.ScoredEntry> top = service.selectTopEntries(1);

            // then — CHEAP 배지(+100) + ACTIVE(+20) + 대단지(+10) > UPCOMING
            assertThat(top).hasSize(1);
            assertThat(top.get(0).subscription().getHouseName()).isEqualTo("저렴아파트");
        }
    }

    private Subscription createSubscription(Long id, String houseName, String area,
                                             LocalDate receiptStart, LocalDate receiptEnd) {
        return Subscription.builder()
                .id(id)
                .houseName(houseName)
                .area(area)
                .source("ApplyHome")
                .receiptStartDate(receiptStart)
                .receiptEndDate(receiptEnd)
                .build();
    }

    // Mockito eq() stub for int
    private static int eq(int val) {
        return org.mockito.ArgumentMatchers.eq(val);
    }
}
