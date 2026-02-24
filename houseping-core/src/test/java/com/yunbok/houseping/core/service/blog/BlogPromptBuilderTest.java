package com.yunbok.houseping.core.service.blog;

import com.yunbok.houseping.core.domain.Subscription;
import com.yunbok.houseping.core.domain.SubscriptionPrice;
import com.yunbok.houseping.support.dto.PriceBadge;
import com.yunbok.houseping.support.dto.SubscriptionCardView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BlogPromptBuilder - 블로그 프롬프트 생성")
class BlogPromptBuilderTest {

    private BlogPromptBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new BlogPromptBuilder();
    }

    @Nested
    @DisplayName("build() - 프롬프트 생성")
    class Build {

        @Test
        @DisplayName("정상적으로 프롬프트를 생성한다")
        void buildsPromptSuccessfully() {
            // given
            Subscription sub = Subscription.builder()
                    .id(1L).houseName("테스트아파트").area("서울").source("ApplyHome")
                    .address("서울시 강남구 역삼동 123")
                    .totalSupplyCount(500).houseType("APT")
                    .receiptStartDate(LocalDate.of(2026, 3, 1))
                    .receiptEndDate(LocalDate.of(2026, 3, 5))
                    .build();
            SubscriptionCardView card = SubscriptionCardView.builder()
                    .subscription(sub).priceBadge(PriceBadge.CHEAP).build();
            SubscriptionPrice price = SubscriptionPrice.builder()
                    .houseType("084T").topAmount(50000L).build();

            LocalDate weekStart = LocalDate.of(2026, 3, 2);
            LocalDate weekEnd = LocalDate.of(2026, 3, 8);

            // when
            String prompt = builder.build(List.of(card), Map.of(1L, price), 3, weekStart, weekEnd);

            // then
            assertThat(prompt).contains("부동산 청약 분석 전문가");
            assertThat(prompt).contains("테스트아파트");
            assertThat(prompt).contains("서울");
            assertThat(prompt).contains("500세대");
            assertThat(prompt).contains("대표 분양가");
            assertThat(prompt).contains("저렴 (시세 대비 저렴)");
            assertThat(prompt).contains("TOP 3");
        }

        @Test
        @DisplayName("분양가가 없는 청약도 프롬프트에 포함한다")
        void includesSubscriptionsWithoutPrice() {
            // given
            Subscription sub = Subscription.builder()
                    .id(1L).houseName("가격없음아파트").area("경기").source("ApplyHome")
                    .receiptStartDate(LocalDate.of(2026, 3, 1))
                    .build();
            SubscriptionCardView card = SubscriptionCardView.builder()
                    .subscription(sub).priceBadge(PriceBadge.UNKNOWN).build();

            LocalDate weekStart = LocalDate.of(2026, 3, 2);
            LocalDate weekEnd = LocalDate.of(2026, 3, 8);

            // when
            String prompt = builder.build(List.of(card), Map.of(), 3, weekStart, weekEnd);

            // then
            assertThat(prompt).contains("가격없음아파트");
            assertThat(prompt).contains("판단 불가");
            assertThat(prompt).doesNotContain("대표 분양가");
        }

        @Test
        @DisplayName("빈 리스트로도 프롬프트를 생성한다")
        void buildsPromptWithEmptyList() {
            // when
            String prompt = builder.build(List.of(), Map.of(), 3,
                    LocalDate.of(2026, 3, 2), LocalDate.of(2026, 3, 8));

            // then
            assertThat(prompt).contains("총 0건");
            assertThat(prompt).contains("TOP 3");
        }
    }
}
