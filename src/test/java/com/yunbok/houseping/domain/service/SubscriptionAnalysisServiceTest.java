package com.yunbok.houseping.domain.service;

import com.yunbok.houseping.domain.model.*;
import com.yunbok.houseping.domain.port.out.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("SubscriptionAnalysisService - 청약 분석 서비스")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SubscriptionAnalysisServiceTest {

    @Mock
    private SubscriptionQueryPort subscriptionQueryPort;

    @Mock
    private SubscriptionPriceQueryPort subscriptionPriceQueryPort;

    @Mock
    private RealTransactionQueryPort realTransactionQueryPort;

    @Mock
    private RealTransactionFetchPort realTransactionFetchPort;

    @Mock
    private RegionCodeQueryPort regionCodeQueryPort;

    private AddressHelper addressParser;
    private HouseTypeComparisonBuilder comparisonBuilder;
    private MarketAnalyzer marketAnalyzer;
    private SubscriptionAnalysisService analysisService;

    @BeforeEach
    void setUp() {
        addressParser = new AddressHelper(regionCodeQueryPort);
        comparisonBuilder = new HouseTypeComparisonBuilder();
        marketAnalyzer = new MarketAnalyzer();
        analysisService = new SubscriptionAnalysisService(
                subscriptionQueryPort,
                subscriptionPriceQueryPort,
                realTransactionQueryPort,
                realTransactionFetchPort,
                addressParser,
                comparisonBuilder,
                marketAnalyzer
        );
    }

    @Nested
    @DisplayName("analyze() - 청약 분석")
    class Analyze {

        @Test
        @DisplayName("청약 정보가 없으면 예외를 던진다")
        void throwsExceptionWhenSubscriptionNotFound() {
            // given
            when(subscriptionQueryPort.findById(1L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> analysisService.analyze(1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("청약 정보를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("주소에서 법정동코드를 추출하여 분석한다")
        void extractsLawdCdFromAddress() {
            // given
            Subscription subscription = createSubscription("서울특별시 강남구 테헤란로 123");

            when(subscriptionQueryPort.findById(1L)).thenReturn(Optional.of(subscription));
            when(regionCodeQueryPort.findLawdCd("서울특별시", "강남구"))
                    .thenReturn(Optional.of("11680"));
            when(realTransactionQueryPort.findByLawdCd("11680"))
                    .thenReturn(createTransactionList());
            when(subscriptionPriceQueryPort.findByHouseManageNo(any()))
                    .thenReturn(Collections.emptyList());

            // when
            SubscriptionAnalysisResult result = analysisService.analyze(1L);

            // then
            assertThat(result.getSubscription()).isEqualTo(subscription);
        }

        @Test
        @DisplayName("법정동코드를 찾을 수 없으면 시세 분석 없이 반환한다")
        void returnsWithoutMarketAnalysisWhenNoLawdCd() {
            // given
            Subscription subscription = createSubscription("알 수 없는 주소");

            when(subscriptionQueryPort.findById(1L)).thenReturn(Optional.of(subscription));
            when(subscriptionPriceQueryPort.findByHouseManageNo(any()))
                    .thenReturn(Collections.emptyList());

            // when
            SubscriptionAnalysisResult result = analysisService.analyze(1L);

            // then
            assertThat(result.getMarketAnalysis()).isNull();
            assertThat(result.getRecentTransactions()).isEmpty();
        }

        @Test
        @DisplayName("실거래 데이터가 있으면 시장 분석을 수행한다")
        void performsMarketAnalysisWithTransactions() {
            // given - 복합 시군구 패턴 테스트 (수원시 팔달구 → 수원시팔달구)
            Subscription subscription = createSubscription("경기도 수원시 팔달구 인계동");
            List<RealTransaction> transactions = createTransactionList();

            when(subscriptionQueryPort.findById(1L)).thenReturn(Optional.of(subscription));
            // 복합 시군구는 findLawdCdByContaining으로 조회됨
            when(regionCodeQueryPort.findLawdCdByContaining("수원시팔달구"))
                    .thenReturn(Optional.of("41111"));
            when(realTransactionQueryPort.findByLawdCd("41111"))
                    .thenReturn(transactions);
            when(subscriptionPriceQueryPort.findByHouseManageNo(any()))
                    .thenReturn(Collections.emptyList());

            // when
            SubscriptionAnalysisResult result = analysisService.analyze(1L);

            // then
            assertThat(result.getMarketAnalysis()).isNotNull();
            assertThat(result.getMarketAnalysis().getTransactionCount()).isEqualTo(2);
            assertThat(result.getMarketAnalysis().getAverageAmount()).isEqualTo(95000L);
        }

        @Test
        @DisplayName("분양가 정보가 있으면 주택형별 비교 분석을 수행한다")
        void performsHouseTypeComparison() {
            // given
            Subscription subscription = createSubscription("서울특별시 강남구 역삼동");
            List<RealTransaction> transactions = createTransactionList();
            List<SubscriptionPrice> prices = List.of(
                    SubscriptionPrice.builder()
                            .houseManageNo("12345")
                            .houseType("84A")
                            .topAmount(90000L)
                            .build()
            );

            when(subscriptionQueryPort.findById(1L)).thenReturn(Optional.of(subscription));
            when(regionCodeQueryPort.findLawdCd("서울특별시", "강남구"))
                    .thenReturn(Optional.of("11680"));
            when(realTransactionQueryPort.findByLawdCd("11680"))
                    .thenReturn(transactions);
            when(subscriptionPriceQueryPort.findByHouseManageNo(any()))
                    .thenReturn(prices);

            // when
            SubscriptionAnalysisResult result = analysisService.analyze(1L);

            // then
            assertThat(result.getHouseTypeComparisons()).isNotEmpty();
        }

        @Test
        @DisplayName("단순 시군구 패턴으로 법정동코드를 찾는다")
        void findsLawdCdWithSimplePattern() {
            // given - 동 이름이 일치해야 필터링 통과 (인계동)
            Subscription subscription = createSubscription("서울특별시 강남구 인계동 123");

            when(subscriptionQueryPort.findById(1L)).thenReturn(Optional.of(subscription));
            when(regionCodeQueryPort.findLawdCd(anyString(), anyString()))
                    .thenReturn(Optional.of("11680"));
            when(realTransactionQueryPort.findByLawdCd("11680"))
                    .thenReturn(createTransactionList());
            when(subscriptionPriceQueryPort.findByHouseManageNo(any()))
                    .thenReturn(Collections.emptyList());

            // when
            SubscriptionAnalysisResult result = analysisService.analyze(1L);

            // then
            assertThat(result.getMarketAnalysis()).isNotNull();
        }

        @Test
        @DisplayName("단순 시군구 패턴 실패 시 부분 일치로 재시도한다")
        void retriesWithPartialMatchWhenSimplePatternFails() {
            // given - 동 이름이 일치해야 필터링 통과 (인계동)
            Subscription subscription = createSubscription("서울특별시 강남구 인계동");

            when(subscriptionQueryPort.findById(1L)).thenReturn(Optional.of(subscription));
            when(regionCodeQueryPort.findLawdCd(anyString(), anyString()))
                    .thenReturn(Optional.empty());
            when(regionCodeQueryPort.findLawdCdByContaining(anyString()))
                    .thenReturn(Optional.of("11680"));
            when(realTransactionQueryPort.findByLawdCd("11680"))
                    .thenReturn(createTransactionList());
            when(subscriptionPriceQueryPort.findByHouseManageNo(any()))
                    .thenReturn(Collections.emptyList());

            // when
            SubscriptionAnalysisResult result = analysisService.analyze(1L);

            // then
            assertThat(result.getMarketAnalysis()).isNotNull();
        }

        @Test
        @DisplayName("주소가 null이면 시세 분석 없이 반환한다")
        void returnsWithoutMarketAnalysisWhenAddressIsNull() {
            // given
            Subscription subscription = Subscription.builder()
                    .id(1L)
                    .source("ApplyHome")
                    .houseName("테스트 아파트")
                    .area("서울")
                    .address(null)
                    .receiptStartDate(LocalDate.now())
                    .build();

            when(subscriptionQueryPort.findById(1L)).thenReturn(Optional.of(subscription));
            when(subscriptionPriceQueryPort.findByHouseManageNo(any()))
                    .thenReturn(Collections.emptyList());

            // when
            SubscriptionAnalysisResult result = analysisService.analyze(1L);

            // then
            assertThat(result.getMarketAnalysis()).isNull();
        }

        @Test
        @DisplayName("시세는 유사 면적 거래의 평균으로 계산한다")
        void calculatesMarketPriceAsAverage() {
            // given
            Subscription subscription = createSubscription("서울특별시 강남구 인계동");
            List<RealTransaction> transactions = List.of(
                    RealTransaction.builder()
                            .lawdCd("11680")
                            .aptName("아파트1")
                            .dealAmount(100000L)
                            .exclusiveArea(BigDecimal.valueOf(84))
                            .dealDate(LocalDate.now())
                            .dongName("인계동")
                            .build(),
                    RealTransaction.builder()
                            .lawdCd("11680")
                            .aptName("아파트2")
                            .dealAmount(90000L)
                            .exclusiveArea(BigDecimal.valueOf(85))
                            .dealDate(LocalDate.now().minusDays(1))
                            .dongName("인계동")
                            .build(),
                    RealTransaction.builder()
                            .lawdCd("11680")
                            .aptName("아파트3")
                            .dealAmount(110000L)
                            .exclusiveArea(BigDecimal.valueOf(83))
                            .dealDate(LocalDate.now().minusDays(2))
                            .dongName("인계동")
                            .build()
            );
            List<SubscriptionPrice> prices = List.of(
                    SubscriptionPrice.builder()
                            .houseManageNo("12345")
                            .houseType("84A")
                            .topAmount(95000L)
                            .build()
            );

            when(subscriptionQueryPort.findById(1L)).thenReturn(Optional.of(subscription));
            when(regionCodeQueryPort.findLawdCd("서울특별시", "강남구"))
                    .thenReturn(Optional.of("11680"));
            when(realTransactionQueryPort.findByLawdCd("11680"))
                    .thenReturn(transactions);
            when(subscriptionPriceQueryPort.findByHouseManageNo(any()))
                    .thenReturn(prices);

            // when
            SubscriptionAnalysisResult result = analysisService.analyze(1L);

            // then - 평균: (100000 + 90000 + 110000) / 3 = 100000
            assertThat(result.getHouseTypeComparisons()).hasSize(1);
            HouseTypeComparison comparison = result.getHouseTypeComparisons().get(0);
            assertThat(comparison.getMarketPrice()).isEqualTo(100000L);
            assertThat(comparison.getEstimatedProfit()).isEqualTo(5000L); // 100000 - 95000
        }

        @Test
        @DisplayName("주택형에서 면적을 추출할 수 없으면 비교에서 제외한다")
        void skipsHouseTypeWhenAreaCannotBeExtracted() {
            // given
            Subscription subscription = createSubscription("서울특별시 강남구 역삼동");
            List<SubscriptionPrice> prices = List.of(
                    SubscriptionPrice.builder()
                            .houseManageNo("12345")
                            .houseType("INVALID")  // 숫자가 없는 타입
                            .topAmount(90000L)
                            .build()
            );

            when(subscriptionQueryPort.findById(1L)).thenReturn(Optional.of(subscription));
            when(regionCodeQueryPort.findLawdCd("서울특별시", "강남구"))
                    .thenReturn(Optional.of("11680"));
            when(realTransactionQueryPort.findByLawdCd("11680"))
                    .thenReturn(createTransactionList());
            when(subscriptionPriceQueryPort.findByHouseManageNo(any()))
                    .thenReturn(prices);

            // when
            SubscriptionAnalysisResult result = analysisService.analyze(1L);

            // then
            assertThat(result.getHouseTypeComparisons()).isEmpty();
        }
    }

    @Nested
    @DisplayName("MarketAnalysis - 시장 분석 결과")
    class MarketAnalysisTest {

        @Test
        @DisplayName("평균 거래가를 억 단위로 포맷한다")
        void formatsAverageAmountInEok() {
            // given
            MarketAnalysis analysis = MarketAnalysis.builder()
                    .averageAmount(120000L)
                    .maxAmount(150000L)
                    .minAmount(90000L)
                    .averagePricePerPyeong(3500L)
                    .transactionCount(10)
                    .build();

            // then
            assertThat(analysis.getAverageAmountFormatted()).isEqualTo("12억");
            assertThat(analysis.getMaxAmountFormatted()).isEqualTo("15억");
            assertThat(analysis.getMinAmountFormatted()).isEqualTo("9억");
        }

        @Test
        @DisplayName("1억 미만은 만원 단위로 포맷한다")
        void formatsAmountUnderEokInMan() {
            // given
            MarketAnalysis analysis = MarketAnalysis.builder()
                    .averageAmount(5000L)
                    .maxAmount(8000L)
                    .minAmount(3000L)
                    .averagePricePerPyeong(200L)
                    .transactionCount(5)
                    .build();

            // then
            assertThat(analysis.getAverageAmountFormatted()).isEqualTo("5,000만");
        }
    }

    private Subscription createSubscription(String address) {
        return Subscription.builder()
                .id(1L)
                .source("ApplyHome")
                .houseName("테스트 아파트")
                .area("서울")
                .address(address)
                .receiptStartDate(LocalDate.now())
                .build();
    }

    private List<RealTransaction> createTransactionList() {
        return List.of(
                RealTransaction.builder()
                        .lawdCd("11680")
                        .dealYmd("202501")
                        .aptName("아파트1")
                        .dealAmount(100000L)
                        .exclusiveArea(BigDecimal.valueOf(84.5))
                        .floor(10)
                        .dealDate(LocalDate.now())
                        .dongName("인계동")
                        .build(),
                RealTransaction.builder()
                        .lawdCd("11680")
                        .dealYmd("202501")
                        .aptName("아파트2")
                        .dealAmount(90000L)
                        .exclusiveArea(BigDecimal.valueOf(59.9))
                        .floor(5)
                        .dealDate(LocalDate.now().minusDays(10))
                        .dongName("인계동")
                        .build()
        );
    }
}
