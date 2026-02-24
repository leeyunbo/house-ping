package com.yunbok.houseping.core.service.blog;

import com.yunbok.houseping.core.domain.Subscription;
import com.yunbok.houseping.core.domain.RealTransaction;
import com.yunbok.houseping.support.dto.HouseTypeComparison;
import com.yunbok.houseping.support.dto.PriceBadge;
import com.yunbok.houseping.support.dto.SubscriptionAnalysisResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BlogNarrativeBuilder - 블로그 내러티브 생성")
class BlogNarrativeBuilderTest {

    private BlogNarrativeBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new BlogNarrativeBuilder();
    }

    @Nested
    @DisplayName("build() - 내러티브 생성")
    class Build {

        @Test
        @DisplayName("시세 비교 데이터가 있는 경우 차익 정보를 포함한다")
        void includesProfitInfoWithMarketData() {
            // given
            Subscription sub = Subscription.builder()
                    .id(1L).houseName("테스트아파트").area("서울").source("ApplyHome")
                    .totalSupplyCount(500)
                    .receiptStartDate(LocalDate.now().minusDays(1))
                    .receiptEndDate(LocalDate.now().plusDays(5))
                    .build();

            HouseTypeComparison comparison = HouseTypeComparison.builder()
                    .houseType("084T")
                    .supplyArea(new BigDecimal("84"))
                    .supplyPrice(50000L)
                    .marketPrice(60000L)
                    .estimatedProfit(10000L)
                    .similarTransactions(List.of())
                    .build();

            SubscriptionAnalysisResult analysis = SubscriptionAnalysisResult.builder()
                    .subscription(sub)
                    .prices(List.of())
                    .recentTransactions(List.of())
                    .houseTypeComparisons(List.of(comparison))
                    .competitionRates(List.of())
                    .build();

            WeeklyBlogContentService.ScoredEntry entry =
                    new WeeklyBlogContentService.ScoredEntry(sub, analysis, PriceBadge.CHEAP, 120);

            // when
            String narrative = builder.build(entry, 1);

            // then
            assertThat(narrative).contains("1. 테스트아파트 (서울)");
            assertThat(narrative).contains("500세대");
            assertThat(narrative).contains("접수 중");
            assertThat(narrative).contains("분양가는 약 5억 원");
            assertThat(narrative).contains("차익이 예상");
        }

        @Test
        @DisplayName("시세 비교 데이터 없이 기본 정보만 포함한다")
        void buildsNarrativeWithoutMarketData() {
            // given
            Subscription sub = Subscription.builder()
                    .id(1L).houseName("예정아파트").area("경기").source("ApplyHome")
                    .receiptStartDate(LocalDate.now().plusDays(3))
                    .receiptEndDate(LocalDate.now().plusDays(10))
                    .build();

            SubscriptionAnalysisResult analysis = SubscriptionAnalysisResult.builder()
                    .subscription(sub)
                    .prices(List.of())
                    .recentTransactions(List.of())
                    .houseTypeComparisons(List.of())
                    .competitionRates(List.of())
                    .build();

            WeeklyBlogContentService.ScoredEntry entry =
                    new WeeklyBlogContentService.ScoredEntry(sub, analysis, PriceBadge.UNKNOWN, 10);

            // when
            String narrative = builder.build(entry, 2);

            // then
            assertThat(narrative).contains("2. 예정아파트 (경기)");
            assertThat(narrative).contains("접수 예정");
            assertThat(narrative).doesNotContain("분양가는 약");
        }

        @Test
        @DisplayName("시세보다 비싼 분양가를 올바르게 표현한다")
        void describesExpensivePrice() {
            // given
            Subscription sub = Subscription.builder()
                    .id(1L).houseName("비싼아파트").area("서울").source("ApplyHome")
                    .receiptStartDate(LocalDate.now().minusDays(1))
                    .receiptEndDate(LocalDate.now().plusDays(5))
                    .build();

            HouseTypeComparison comparison = HouseTypeComparison.builder()
                    .houseType("084T")
                    .supplyArea(new BigDecimal("84"))
                    .supplyPrice(70000L)
                    .marketPrice(60000L)
                    .estimatedProfit(-10000L)
                    .similarTransactions(List.of())
                    .build();

            SubscriptionAnalysisResult analysis = SubscriptionAnalysisResult.builder()
                    .subscription(sub)
                    .prices(List.of())
                    .recentTransactions(List.of())
                    .houseTypeComparisons(List.of(comparison))
                    .competitionRates(List.of())
                    .build();

            WeeklyBlogContentService.ScoredEntry entry =
                    new WeeklyBlogContentService.ScoredEntry(sub, analysis, PriceBadge.EXPENSIVE, 0);

            // when
            String narrative = builder.build(entry, 3);

            // then
            assertThat(narrative).contains("높은 수준입니다");
        }
    }
}
