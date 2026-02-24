package com.yunbok.houseping.core.service.blog;

import com.yunbok.houseping.core.domain.Subscription;
import com.yunbok.houseping.core.domain.SubscriptionPrice;
import com.yunbok.houseping.core.port.AiContentPort;
import com.yunbok.houseping.core.port.SubscriptionPricePersistencePort;
import com.yunbok.houseping.core.service.subscription.PriceBadgeCalculator;
import com.yunbok.houseping.core.service.subscription.SubscriptionSearchService;
import com.yunbok.houseping.support.dto.BlogContentResult;
import com.yunbok.houseping.support.dto.PriceBadge;
import com.yunbok.houseping.support.dto.SubscriptionCardView;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("AiBlogContentService - AI 블로그 콘텐츠 서비스")
@ExtendWith(MockitoExtension.class)
class AiBlogContentServiceTest {

    @Mock
    private SubscriptionSearchService subscriptionSearchService;

    @Mock
    private SubscriptionPricePersistencePort subscriptionPriceStore;

    @Mock
    private PriceBadgeCalculator priceBadgeCalculator;

    @Mock
    private AiContentPort claudeApiClient;

    @Mock
    private BlogPromptBuilder promptBuilder;

    private AiBlogContentService service;

    @BeforeEach
    void setUp() {
        service = new AiBlogContentService(
                subscriptionSearchService, subscriptionPriceStore,
                priceBadgeCalculator, claudeApiClient, promptBuilder);
    }

    @Nested
    @DisplayName("generateAiBlogContent() - AI 블로그 콘텐츠 생성")
    class GenerateAiBlogContent {

        @Test
        @DisplayName("정상적으로 AI 블로그 콘텐츠를 생성한다")
        void generatesContentSuccessfully() {
            // given
            Subscription sub = Subscription.builder()
                    .id(1L).houseName("테스트아파트").area("서울")
                    .houseManageNo("H001").source("ApplyHome")
                    .receiptStartDate(LocalDate.now()).receiptEndDate(LocalDate.now().plusDays(5))
                    .build();
            SubscriptionCardView card = SubscriptionCardView.builder()
                    .subscription(sub).priceBadge(PriceBadge.CHEAP).build();
            SubscriptionPrice price = SubscriptionPrice.builder()
                    .houseType("084T").topAmount(50000L).supplyCount(100).build();

            when(subscriptionSearchService.getSubscriptionCardsForWeek(any(), any()))
                    .thenReturn(List.of(card));
            when(subscriptionPriceStore.findByHouseManageNo("H001")).thenReturn(List.of(price));
            when(priceBadgeCalculator.selectRepresentativePrice(anyList())).thenReturn(price);
            when(promptBuilder.build(anyList(), anyMap(), eq(3), any(), any())).thenReturn("프롬프트");
            when(claudeApiClient.generateBlogContent("프롬프트")).thenReturn("AI 생성 콘텐츠");

            // when
            BlogContentResult result = service.generateAiBlogContent(3);

            // then
            assertThat(result.getTitle()).contains("TOP 3");
            assertThat(result.getBlogText()).isEqualTo("AI 생성 콘텐츠");
            assertThat(result.getEntries()).isEmpty();
        }

        @Test
        @DisplayName("이번 주 청약이 없으면 예외가 발생한다")
        void throwsWhenNoWeekSubscriptions() {
            // given
            when(subscriptionSearchService.getSubscriptionCardsForWeek(any(), any()))
                    .thenReturn(List.of());

            // when & then
            assertThatThrownBy(() -> service.generateAiBlogContent(3))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("이번 주 분석할 청약 데이터가 없습니다");
        }

        @Test
        @DisplayName("houseManageNo가 없는 청약은 대표 가격 조회를 건너뛴다")
        void skipsSubscriptionsWithoutHouseManageNo() {
            // given
            Subscription subNoHmn = Subscription.builder()
                    .id(1L).houseName("테스트").area("서울").source("ApplyHome")
                    .receiptStartDate(LocalDate.now()).receiptEndDate(LocalDate.now().plusDays(5))
                    .build();  // houseManageNo 없음
            SubscriptionCardView card = SubscriptionCardView.builder()
                    .subscription(subNoHmn).priceBadge(PriceBadge.UNKNOWN).build();

            when(subscriptionSearchService.getSubscriptionCardsForWeek(any(), any()))
                    .thenReturn(List.of(card));
            when(promptBuilder.build(anyList(), anyMap(), eq(3), any(), any())).thenReturn("프롬프트");
            when(claudeApiClient.generateBlogContent("프롬프트")).thenReturn("AI 콘텐츠");

            // when
            BlogContentResult result = service.generateAiBlogContent(3);

            // then
            verify(subscriptionPriceStore, never()).findByHouseManageNo(anyString());
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("대표 가격을 정상적으로 조회한다")
        void buildsRepresentativePricesCorrectly() {
            // given
            Subscription sub1 = Subscription.builder()
                    .id(1L).houseName("아파트1").area("서울").houseManageNo("H001").source("ApplyHome")
                    .receiptStartDate(LocalDate.now()).receiptEndDate(LocalDate.now().plusDays(5)).build();
            Subscription sub2 = Subscription.builder()
                    .id(2L).houseName("아파트2").area("경기").houseManageNo("H002").source("ApplyHome")
                    .receiptStartDate(LocalDate.now()).receiptEndDate(LocalDate.now().plusDays(5)).build();

            SubscriptionCardView card1 = SubscriptionCardView.builder().subscription(sub1).priceBadge(PriceBadge.CHEAP).build();
            SubscriptionCardView card2 = SubscriptionCardView.builder().subscription(sub2).priceBadge(PriceBadge.UNKNOWN).build();

            SubscriptionPrice price1 = SubscriptionPrice.builder().houseType("084T").topAmount(50000L).supplyCount(100).build();

            when(subscriptionSearchService.getSubscriptionCardsForWeek(any(), any()))
                    .thenReturn(List.of(card1, card2));
            when(subscriptionPriceStore.findByHouseManageNo("H001")).thenReturn(List.of(price1));
            when(subscriptionPriceStore.findByHouseManageNo("H002")).thenReturn(List.of());  // 가격 없음
            when(priceBadgeCalculator.selectRepresentativePrice(List.of(price1))).thenReturn(price1);
            when(promptBuilder.build(anyList(), anyMap(), eq(3), any(), any())).thenReturn("프롬프트");
            when(claudeApiClient.generateBlogContent("프롬프트")).thenReturn("AI 콘텐츠");

            // when
            BlogContentResult result = service.generateAiBlogContent(3);

            // then
            assertThat(result).isNotNull();
            verify(subscriptionPriceStore).findByHouseManageNo("H001");
            verify(subscriptionPriceStore).findByHouseManageNo("H002");
        }
    }
}
