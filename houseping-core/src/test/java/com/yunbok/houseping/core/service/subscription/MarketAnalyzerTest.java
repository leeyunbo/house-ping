package com.yunbok.houseping.core.service.subscription;

import com.yunbok.houseping.core.domain.RealTransaction;
import com.yunbok.houseping.support.dto.MarketAnalysis;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MarketAnalyzer - 시장 분석")
class MarketAnalyzerTest {

    private MarketAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new MarketAnalyzer();
    }

    @Nested
    @DisplayName("analyze() - 거래 내역 기반 시장 분석")
    class Analyze {

        @Test
        @DisplayName("빈 리스트를 입력하면 null을 반환한다")
        void returnsNullForEmptyList() {
            // when
            MarketAnalysis result = analyzer.analyze(List.of());

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("정상적인 거래 내역으로 평균/최소/최대를 계산한다")
        void calculatesStatisticsCorrectly() {
            // given
            List<RealTransaction> transactions = List.of(
                    createTransaction(10000L, new BigDecimal("84.0")),
                    createTransaction(20000L, new BigDecimal("84.0")),
                    createTransaction(30000L, new BigDecimal("84.0"))
            );

            // when
            MarketAnalysis result = analyzer.analyze(transactions);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getAverageAmount()).isEqualTo(20000L);
            assertThat(result.getMinAmount()).isEqualTo(10000L);
            assertThat(result.getMaxAmount()).isEqualTo(30000L);
            assertThat(result.getTransactionCount()).isEqualTo(3);
            assertThat(result.getAveragePricePerPyeong()).isPositive();
        }

        @Test
        @DisplayName("단일 거래만 있으면 평균/최소/최대가 동일하다")
        void singleTransactionReturnsEqualStats() {
            // given
            List<RealTransaction> transactions = List.of(
                    createTransaction(15000L, new BigDecimal("84.0"))
            );

            // when
            MarketAnalysis result = analyzer.analyze(transactions);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getAverageAmount()).isEqualTo(15000L);
            assertThat(result.getMinAmount()).isEqualTo(15000L);
            assertThat(result.getMaxAmount()).isEqualTo(15000L);
            assertThat(result.getTransactionCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("pricePerPyeong이 null인 거래는 평균 평당가 계산에서 제외된다")
        void excludesNullPricePerPyeong() {
            // given — exclusiveArea null이면 getPricePerPyeong()이 null
            List<RealTransaction> transactions = List.of(
                    createTransaction(10000L, null),
                    createTransaction(20000L, null)
            );

            // when
            MarketAnalysis result = analyzer.analyze(transactions);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getAveragePricePerPyeong()).isEqualTo(0L);
            assertThat(result.getAverageAmount()).isEqualTo(15000L);
        }
    }

    private RealTransaction createTransaction(Long dealAmount, BigDecimal exclusiveArea) {
        return RealTransaction.builder()
                .dealAmount(dealAmount)
                .exclusiveArea(exclusiveArea)
                .dealDate(LocalDate.now())
                .build();
    }
}
