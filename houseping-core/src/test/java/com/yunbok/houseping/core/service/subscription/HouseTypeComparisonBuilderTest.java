package com.yunbok.houseping.core.service.subscription;

import com.yunbok.houseping.core.domain.RealTransaction;
import com.yunbok.houseping.core.domain.SubscriptionPrice;
import com.yunbok.houseping.support.dto.HouseTypeComparison;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HouseTypeComparisonBuilder - 주택형별 시세 비교")
class HouseTypeComparisonBuilderTest {

    private HouseTypeComparisonBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new HouseTypeComparisonBuilder();
    }

    @Nested
    @DisplayName("build() - 주택형별 시세 비교 생성")
    class Build {

        @Test
        @DisplayName("빈 가격 목록이면 빈 결과를 반환한다")
        void returnsEmptyForEmptyPrices() {
            // given
            List<RealTransaction> transactions = List.of(
                    createTransaction(10000L, new BigDecimal("84.0"))
            );

            // when
            List<HouseTypeComparison> result = builder.build(Collections.emptyList(), transactions);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("정상적인 가격과 거래 내역으로 비교 결과를 생성한다")
        void buildsComparisonCorrectly() {
            // given
            SubscriptionPrice price = SubscriptionPrice.builder()
                    .houseType("084.9543T")
                    .topAmount(50000L)
                    .supplyCount(100)
                    .build();

            List<RealTransaction> transactions = List.of(
                    createTransaction(60000L, new BigDecimal("84.0")),
                    createTransaction(65000L, new BigDecimal("83.0"))
            );

            // when
            List<HouseTypeComparison> result = builder.build(List.of(price), transactions);

            // then
            assertThat(result).hasSize(1);
            HouseTypeComparison comparison = result.get(0);
            assertThat(comparison.getHouseType()).isEqualTo("084.9543T");
            assertThat(comparison.getSupplyPrice()).isEqualTo(50000L);
            assertThat(comparison.getMarketPrice()).isNotNull();
            assertThat(comparison.getEstimatedProfit()).isPositive();
        }

        @Test
        @DisplayName("면적을 추출할 수 없는 주택형은 건너뛴다")
        void skipsWhenAreaNotExtractable() {
            // given
            SubscriptionPrice price = SubscriptionPrice.builder()
                    .houseType("TypeABC")
                    .topAmount(50000L)
                    .build();

            List<RealTransaction> transactions = List.of(
                    createTransaction(60000L, new BigDecimal("84.0"))
            );

            // when
            List<HouseTypeComparison> result = builder.build(List.of(price), transactions);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("매칭되는 유사 면적 거래가 없으면 시세가 null이다")
        void returnsNullMarketPriceWhenNoMatch() {
            // given
            SubscriptionPrice price = SubscriptionPrice.builder()
                    .houseType("084.9543T")
                    .topAmount(50000L)
                    .build();

            List<RealTransaction> transactions = List.of(
                    createTransaction(60000L, new BigDecimal("50.0"))  // 84 ± 5 범위 밖
            );

            // when
            List<HouseTypeComparison> result = builder.build(List.of(price), transactions);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getMarketPrice()).isNull();
            assertThat(result.get(0).getEstimatedProfit()).isNull();
        }
    }

    @Nested
    @DisplayName("extractAreaFromHouseType() - 주택형에서 면적 추출")
    class ExtractArea {

        @Test
        @DisplayName("정상적인 주택형에서 면적을 추출한다")
        void extractsAreaCorrectly() {
            // when & then
            assertThat(builder.extractAreaFromHouseType("084.9543T")).isEqualTo(new BigDecimal("84"));
        }

        @Test
        @DisplayName("null을 입력하면 null을 반환한다")
        void returnsNullForNull() {
            // when & then
            assertThat(builder.extractAreaFromHouseType(null)).isNull();
        }

        @Test
        @DisplayName("숫자가 없는 문자열이면 null을 반환한다")
        void returnsNullForNonNumeric() {
            // when & then
            assertThat(builder.extractAreaFromHouseType("TypeABC")).isNull();
        }

        @Test
        @DisplayName("선행 0을 제거한다")
        void removesLeadingZeros() {
            // when & then
            assertThat(builder.extractAreaFromHouseType("059.9876A")).isEqualTo(new BigDecimal("59"));
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
