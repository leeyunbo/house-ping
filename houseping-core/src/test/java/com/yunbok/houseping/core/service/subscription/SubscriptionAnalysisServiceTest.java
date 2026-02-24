package com.yunbok.houseping.core.service.subscription;

import com.yunbok.houseping.core.domain.RealTransaction;
import com.yunbok.houseping.core.domain.Subscription;
import com.yunbok.houseping.core.domain.SubscriptionPrice;
import com.yunbok.houseping.core.port.RealTransactionFetchPort;
import com.yunbok.houseping.core.port.RealTransactionPersistencePort;
import com.yunbok.houseping.core.port.SubscriptionPersistencePort;
import com.yunbok.houseping.core.port.SubscriptionPricePersistencePort;
import com.yunbok.houseping.entity.CompetitionRateEntity;
import com.yunbok.houseping.repository.CompetitionRateRepository;
import com.yunbok.houseping.support.dto.MarketAnalysis;
import com.yunbok.houseping.support.dto.SubscriptionAnalysisResult;
import com.yunbok.houseping.support.util.AddressHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("SubscriptionAnalysisService - 청약 분석 서비스")
@ExtendWith(MockitoExtension.class)
class SubscriptionAnalysisServiceTest {

    @Mock
    private SubscriptionPersistencePort subscriptionQueryPort;

    @Mock
    private SubscriptionPricePersistencePort subscriptionPriceQueryPort;

    @Mock
    private RealTransactionPersistencePort realTransactionQueryPort;

    @Mock
    private RealTransactionFetchPort realTransactionFetchPort;

    @Mock
    private CompetitionRateRepository competitionRateRepository;

    @Mock
    private AddressHelper addressParser;

    @Mock
    private HouseTypeComparisonBuilder comparisonBuilder;

    @Mock
    private MarketAnalyzer marketAnalyzer;

    private SubscriptionAnalysisService service;

    @BeforeEach
    void setUp() {
        service = new SubscriptionAnalysisService(
                subscriptionQueryPort, subscriptionPriceQueryPort,
                realTransactionQueryPort, realTransactionFetchPort,
                competitionRateRepository, addressParser, comparisonBuilder, marketAnalyzer);
    }

    @Nested
    @DisplayName("analyze() - 청약 분석")
    class Analyze {

        @Test
        @DisplayName("정상적으로 전체 분석 결과를 생성한다")
        void analyzesFullResult() {
            // given
            Subscription sub = createSubscription(1L, "H001", "서울시 강남구 역삼동 123");
            int currentYear = LocalDate.now().getYear();
            RealTransaction tx = createTransaction(60000L, new BigDecimal("84.0"), currentYear);

            when(subscriptionQueryPort.findById(1L)).thenReturn(Optional.of(sub));
            when(addressParser.extractLawdCd("서울시 강남구 역삼동 123")).thenReturn("11680");
            when(addressParser.extractDongName("서울시 강남구 역삼동 123")).thenReturn("역삼동");
            when(realTransactionQueryPort.findByLawdCd("11680")).thenReturn(List.of(tx));
            when(addressParser.filterByDongName(anyList(), eq("역삼동"))).thenReturn(List.of(tx));

            SubscriptionPrice price = SubscriptionPrice.builder().houseType("084T").topAmount(50000L).build();
            when(subscriptionPriceQueryPort.findByHouseManageNo("H001")).thenReturn(List.of(price));
            when(comparisonBuilder.build(anyList(), anyList())).thenReturn(List.of());
            when(marketAnalyzer.analyze(anyList())).thenReturn(
                    MarketAnalysis.builder().transactionCount(1).averageAmount(60000L).build());
            when(competitionRateRepository.findByHouseManageNo("H001")).thenReturn(List.of(
                    CompetitionRateEntity.builder()
                            .houseManageNo("H001").houseType("084T")
                            .rank(1).residenceArea("해당지역")
                            .supplyCount(10).requestCount(50)
                            .competitionRate(new BigDecimal("5.0"))
                            .build()
            ));

            // when
            SubscriptionAnalysisResult result = service.analyze(1L);

            // then
            assertThat(result.getSubscription()).isEqualTo(sub);
            assertThat(result.getDongName()).isEqualTo("역삼동");
            assertThat(result.isNewBuildBased()).isTrue();
            assertThat(result.hasMarketAnalysis()).isTrue();
            assertThat(result.hasCompetitionRates()).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 청약 ID로 분석하면 예외가 발생한다")
        void throwsWhenSubscriptionNotFound() {
            // given
            when(subscriptionQueryPort.findById(99L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.analyze(99L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("청약 정보를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("캐시에 없으면 API를 호출하여 거래 내역을 조회한다")
        void fetchesFromApiWhenNotCached() {
            // given
            Subscription sub = createSubscription(1L, "H001", "서울시 강남구 역삼동 123");
            RealTransaction tx = createTransaction(50000L, new BigDecimal("84.0"), 2020);

            when(subscriptionQueryPort.findById(1L)).thenReturn(Optional.of(sub));
            when(addressParser.extractLawdCd(anyString())).thenReturn("11680");
            when(addressParser.extractDongName(anyString())).thenReturn("역삼동");
            when(realTransactionQueryPort.findByLawdCd("11680")).thenReturn(List.of());  // 캐시 없음
            when(realTransactionFetchPort.fetchAndCacheRecentTransactions("11680", 6)).thenReturn(List.of(tx));
            when(addressParser.filterByDongName(anyList(), eq("역삼동"))).thenReturn(List.of(tx));
            when(subscriptionPriceQueryPort.findByHouseManageNo("H001")).thenReturn(List.of());
            when(marketAnalyzer.analyze(anyList())).thenReturn(null);
            when(competitionRateRepository.findByHouseManageNo("H001")).thenReturn(List.of());

            // when
            SubscriptionAnalysisResult result = service.analyze(1L);

            // then
            verify(realTransactionFetchPort).fetchAndCacheRecentTransactions("11680", 6);
            assertThat(result.getRecentTransactions()).hasSize(1);
        }

        @Test
        @DisplayName("lawdCd가 null이면 빈 거래 내역을 반환한다")
        void returnsEmptyTransactionsWhenLawdCdNull() {
            // given
            Subscription sub = createSubscription(1L, "H001", "알수없는주소");
            when(subscriptionQueryPort.findById(1L)).thenReturn(Optional.of(sub));
            when(addressParser.extractLawdCd("알수없는주소")).thenReturn(null);
            when(addressParser.extractDongName("알수없는주소")).thenReturn(null);
            when(addressParser.filterByDongName(anyList(), isNull())).thenReturn(List.of());
            when(subscriptionPriceQueryPort.findByHouseManageNo("H001")).thenReturn(List.of());
            when(marketAnalyzer.analyze(anyList())).thenReturn(null);
            when(competitionRateRepository.findByHouseManageNo("H001")).thenReturn(List.of());

            // when
            SubscriptionAnalysisResult result = service.analyze(1L);

            // then
            assertThat(result.getRecentTransactions()).isEmpty();
            assertThat(result.isNewBuildBased()).isFalse();
            verify(realTransactionQueryPort, never()).findByLawdCd(anyString());
        }

        @Test
        @DisplayName("신축 거래가 없으면 newBuildBased가 false이다")
        void newBuildBasedFalseWhenNoNewBuild() {
            // given
            Subscription sub = createSubscription(1L, "H001", "서울시 강남구 역삼동 123");
            RealTransaction oldTx = createTransaction(50000L, new BigDecimal("84.0"), 2000);

            when(subscriptionQueryPort.findById(1L)).thenReturn(Optional.of(sub));
            when(addressParser.extractLawdCd(anyString())).thenReturn("11680");
            when(addressParser.extractDongName(anyString())).thenReturn("역삼동");
            when(realTransactionQueryPort.findByLawdCd("11680")).thenReturn(List.of(oldTx));
            when(addressParser.filterByDongName(anyList(), eq("역삼동"))).thenReturn(List.of(oldTx));
            when(subscriptionPriceQueryPort.findByHouseManageNo("H001")).thenReturn(List.of());
            when(marketAnalyzer.analyze(anyList())).thenReturn(null);
            when(competitionRateRepository.findByHouseManageNo("H001")).thenReturn(List.of());

            // when
            SubscriptionAnalysisResult result = service.analyze(1L);

            // then
            assertThat(result.isNewBuildBased()).isFalse();
            assertThat(result.getHouseTypeComparisons()).isEmpty();
        }

        @Test
        @DisplayName("houseManageNo가 없으면 분양가 조회를 건너뛴다")
        void skipsPriceQueryWhenNoHouseManageNo() {
            // given
            Subscription sub = Subscription.builder()
                    .id(1L).houseName("테스트").area("서울").address("서울시 강남구").build();

            when(subscriptionQueryPort.findById(1L)).thenReturn(Optional.of(sub));
            when(addressParser.extractLawdCd(anyString())).thenReturn(null);
            when(addressParser.extractDongName(anyString())).thenReturn(null);
            when(addressParser.filterByDongName(anyList(), isNull())).thenReturn(List.of());
            when(marketAnalyzer.analyze(anyList())).thenReturn(null);

            // when
            SubscriptionAnalysisResult result = service.analyze(1L);

            // then
            assertThat(result.getPrices()).isEmpty();
            verify(subscriptionPriceQueryPort, never()).findByHouseManageNo(anyString());
        }

        @Test
        @DisplayName("경쟁률 정보를 정렬하여 반환한다")
        void sortsCompetitionRates() {
            // given
            Subscription sub = createSubscription(1L, "H001", "서울시 강남구 역삼동 123");

            when(subscriptionQueryPort.findById(1L)).thenReturn(Optional.of(sub));
            when(addressParser.extractLawdCd(anyString())).thenReturn(null);
            when(addressParser.extractDongName(anyString())).thenReturn(null);
            when(addressParser.filterByDongName(anyList(), isNull())).thenReturn(List.of());
            when(subscriptionPriceQueryPort.findByHouseManageNo("H001")).thenReturn(List.of());
            when(marketAnalyzer.analyze(anyList())).thenReturn(null);
            when(competitionRateRepository.findByHouseManageNo("H001")).thenReturn(List.of(
                    CompetitionRateEntity.builder()
                            .houseType("084T").rank(2).residenceArea("기타지역")
                            .supplyCount(10).requestCount(20).competitionRate(new BigDecimal("2.0")).build(),
                    CompetitionRateEntity.builder()
                            .houseType("084T").rank(1).residenceArea("해당지역")
                            .supplyCount(10).requestCount(50).competitionRate(new BigDecimal("5.0")).build()
            ));

            // when
            SubscriptionAnalysisResult result = service.analyze(1L);

            // then
            assertThat(result.getCompetitionRates()).hasSize(2);
            assertThat(result.getCompetitionRates().get(0).getRank()).isEqualTo(1);
            assertThat(result.getCompetitionRates().get(0).getResidenceArea()).isEqualTo("해당지역");
        }
    }

    private Subscription createSubscription(Long id, String houseManageNo, String address) {
        return Subscription.builder()
                .id(id)
                .houseManageNo(houseManageNo)
                .address(address)
                .area("서울")
                .houseName("테스트아파트")
                .source("ApplyHome")
                .build();
    }

    private RealTransaction createTransaction(Long dealAmount, BigDecimal exclusiveArea, int buildYear) {
        return RealTransaction.builder()
                .dealAmount(dealAmount)
                .exclusiveArea(exclusiveArea)
                .buildYear(buildYear)
                .dealDate(LocalDate.now())
                .build();
    }
}
