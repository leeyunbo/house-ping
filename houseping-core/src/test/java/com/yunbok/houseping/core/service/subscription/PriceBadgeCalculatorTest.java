package com.yunbok.houseping.core.service.subscription;

import com.yunbok.houseping.core.domain.RealTransaction;
import com.yunbok.houseping.core.domain.Subscription;
import com.yunbok.houseping.core.domain.SubscriptionPrice;
import com.yunbok.houseping.core.port.RealTransactionPersistencePort;
import com.yunbok.houseping.core.port.SubscriptionPricePersistencePort;
import com.yunbok.houseping.support.dto.PriceBadge;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@DisplayName("PriceBadgeCalculator - 가격 배지 계산")
@ExtendWith(MockitoExtension.class)
class PriceBadgeCalculatorTest {

    @Mock
    private SubscriptionPricePersistencePort subscriptionPriceQueryPort;

    @Mock
    private RealTransactionPersistencePort realTransactionQueryPort;

    @Mock
    private AddressHelper addressHelper;

    private HouseTypeComparisonBuilder comparisonBuilder;
    private PriceBadgeCalculator calculator;

    @BeforeEach
    void setUp() {
        comparisonBuilder = new HouseTypeComparisonBuilder();
        calculator = new PriceBadgeCalculator(subscriptionPriceQueryPort, realTransactionQueryPort, addressHelper, comparisonBuilder);
    }

    @Nested
    @DisplayName("computePriceBadge() - 가격 배지 계산")
    class ComputePriceBadge {

        @Test
        @DisplayName("시세 대비 싼 분양가는 CHEAP을 반환한다")
        void returnsCheapWhenLowerThanMarket() {
            // given
            Subscription sub = createSubscription("H001", "서울시 강남구 역삼동 123", "ApplyHome");
            SubscriptionPrice price = createPrice("084.9543T", 40000L, 100);
            int currentYear = LocalDate.now().getYear();

            when(subscriptionPriceQueryPort.findByHouseManageNo("H001")).thenReturn(List.of(price));
            when(addressHelper.extractLawdCd("서울시 강남구 역삼동 123")).thenReturn("11680");
            when(addressHelper.extractDongName("서울시 강남구 역삼동 123")).thenReturn("역삼동");
            when(realTransactionQueryPort.findByLawdCd("11680")).thenReturn(List.of(
                    createTransaction(60000L, new BigDecimal("84.0"), currentYear),
                    createTransaction(65000L, new BigDecimal("83.0"), currentYear)
            ));
            when(addressHelper.filterByDongName(anyList(), eq("역삼동"))).thenAnswer(inv -> inv.getArgument(0));

            // when
            PriceBadge result = calculator.computePriceBadge(sub);

            // then
            assertThat(result).isEqualTo(PriceBadge.CHEAP);
        }

        @Test
        @DisplayName("시세 대비 비싼 분양가는 EXPENSIVE를 반환한다")
        void returnsExpensiveWhenHigherThanMarket() {
            // given
            Subscription sub = createSubscription("H001", "서울시 강남구 역삼동 123", "ApplyHome");
            SubscriptionPrice price = createPrice("084.9543T", 70000L, 100);
            int currentYear = LocalDate.now().getYear();

            when(subscriptionPriceQueryPort.findByHouseManageNo("H001")).thenReturn(List.of(price));
            when(addressHelper.extractLawdCd("서울시 강남구 역삼동 123")).thenReturn("11680");
            when(addressHelper.extractDongName("서울시 강남구 역삼동 123")).thenReturn("역삼동");
            when(realTransactionQueryPort.findByLawdCd("11680")).thenReturn(List.of(
                    createTransaction(50000L, new BigDecimal("84.0"), currentYear),
                    createTransaction(55000L, new BigDecimal("83.0"), currentYear)
            ));
            when(addressHelper.filterByDongName(anyList(), eq("역삼동"))).thenAnswer(inv -> inv.getArgument(0));

            // when
            PriceBadge result = calculator.computePriceBadge(sub);

            // then
            assertThat(result).isEqualTo(PriceBadge.EXPENSIVE);
        }

        @Test
        @DisplayName("LH 청약은 UNKNOWN을 반환한다")
        void returnsUnknownForLH() {
            // given
            Subscription sub = createSubscription("H001", "서울시 강남구", "LH");

            // when
            PriceBadge result = calculator.computePriceBadge(sub);

            // then
            assertThat(result).isEqualTo(PriceBadge.UNKNOWN);
        }

        @Test
        @DisplayName("houseManageNo가 없으면 UNKNOWN을 반환한다")
        void returnsUnknownWhenNoHouseManageNo() {
            // given
            Subscription sub = Subscription.builder()
                    .source("ApplyHome")
                    .address("서울시 강남구")
                    .build();

            // when
            PriceBadge result = calculator.computePriceBadge(sub);

            // then
            assertThat(result).isEqualTo(PriceBadge.UNKNOWN);
        }

        @Test
        @DisplayName("분양가 정보가 없으면 UNKNOWN을 반환한다")
        void returnsUnknownWhenNoPrices() {
            // given
            Subscription sub = createSubscription("H001", "서울시 강남구", "ApplyHome");
            when(subscriptionPriceQueryPort.findByHouseManageNo("H001")).thenReturn(List.of());

            // when
            PriceBadge result = calculator.computePriceBadge(sub);

            // then
            assertThat(result).isEqualTo(PriceBadge.UNKNOWN);
        }

        @Test
        @DisplayName("면적을 추출할 수 없으면 UNKNOWN을 반환한다")
        void returnsUnknownWhenAreaNotExtractable() {
            // given
            Subscription sub = createSubscription("H001", "서울시 강남구", "ApplyHome");
            SubscriptionPrice price = createPrice("TypeABC", 50000L, 100);
            when(subscriptionPriceQueryPort.findByHouseManageNo("H001")).thenReturn(List.of(price));

            // when
            PriceBadge result = calculator.computePriceBadge(sub);

            // then
            assertThat(result).isEqualTo(PriceBadge.UNKNOWN);
        }

        @Test
        @DisplayName("lawdCd가 null이면 UNKNOWN을 반환한다")
        void returnsUnknownWhenLawdCdNull() {
            // given
            Subscription sub = createSubscription("H001", "알수없는주소", "ApplyHome");
            SubscriptionPrice price = createPrice("084.9543T", 50000L, 100);
            when(subscriptionPriceQueryPort.findByHouseManageNo("H001")).thenReturn(List.of(price));
            when(addressHelper.extractLawdCd("알수없는주소")).thenReturn(null);

            // when
            PriceBadge result = calculator.computePriceBadge(sub);

            // then
            assertThat(result).isEqualTo(PriceBadge.UNKNOWN);
        }

        @Test
        @DisplayName("신축 거래가 없으면 UNKNOWN을 반환한다")
        void returnsUnknownWhenNoNewBuild() {
            // given
            Subscription sub = createSubscription("H001", "서울시 강남구 역삼동 123", "ApplyHome");
            SubscriptionPrice price = createPrice("084.9543T", 50000L, 100);

            when(subscriptionPriceQueryPort.findByHouseManageNo("H001")).thenReturn(List.of(price));
            when(addressHelper.extractLawdCd(anyString())).thenReturn("11680");
            when(addressHelper.extractDongName(anyString())).thenReturn("역삼동");
            when(realTransactionQueryPort.findByLawdCd("11680")).thenReturn(List.of(
                    createTransaction(50000L, new BigDecimal("84.0"), 2000)  // 매우 오래된 건물
            ));
            when(addressHelper.filterByDongName(anyList(), eq("역삼동"))).thenAnswer(inv -> inv.getArgument(0));

            // when
            PriceBadge result = calculator.computePriceBadge(sub);

            // then
            assertThat(result).isEqualTo(PriceBadge.UNKNOWN);
        }

        @Test
        @DisplayName("유사 면적 거래가 없으면 UNKNOWN을 반환한다")
        void returnsUnknownWhenNoSimilarAreaTransactions() {
            // given
            Subscription sub = createSubscription("H001", "서울시 강남구 역삼동 123", "ApplyHome");
            SubscriptionPrice price = createPrice("084.9543T", 50000L, 100);
            int currentYear = LocalDate.now().getYear();

            when(subscriptionPriceQueryPort.findByHouseManageNo("H001")).thenReturn(List.of(price));
            when(addressHelper.extractLawdCd(anyString())).thenReturn("11680");
            when(addressHelper.extractDongName(anyString())).thenReturn("역삼동");
            when(realTransactionQueryPort.findByLawdCd("11680")).thenReturn(List.of(
                    createTransaction(50000L, new BigDecimal("30.0"), currentYear)  // 면적 차이 큼
            ));
            when(addressHelper.filterByDongName(anyList(), eq("역삼동"))).thenAnswer(inv -> inv.getArgument(0));

            // when
            PriceBadge result = calculator.computePriceBadge(sub);

            // then
            assertThat(result).isEqualTo(PriceBadge.UNKNOWN);
        }

        @Test
        @DisplayName("짝수 개 거래의 중앙값을 올바르게 계산한다")
        void calculatesMedianForEvenCount() {
            // given
            Subscription sub = createSubscription("H001", "서울시 강남구 역삼동 123", "ApplyHome");
            SubscriptionPrice price = createPrice("084.9543T", 40000L, 100);
            int currentYear = LocalDate.now().getYear();

            when(subscriptionPriceQueryPort.findByHouseManageNo("H001")).thenReturn(List.of(price));
            when(addressHelper.extractLawdCd(anyString())).thenReturn("11680");
            when(addressHelper.extractDongName(anyString())).thenReturn("역삼동");
            // 중앙값 = (50000 + 60000) / 2 = 55000, 40000 < 55000 * 0.95 = 52250 → CHEAP
            when(realTransactionQueryPort.findByLawdCd("11680")).thenReturn(List.of(
                    createTransaction(50000L, new BigDecimal("84.0"), currentYear),
                    createTransaction(60000L, new BigDecimal("83.0"), currentYear)
            ));
            when(addressHelper.filterByDongName(anyList(), eq("역삼동"))).thenAnswer(inv -> inv.getArgument(0));

            // when
            PriceBadge result = calculator.computePriceBadge(sub);

            // then
            assertThat(result).isEqualTo(PriceBadge.CHEAP);
        }
    }

    @Nested
    @DisplayName("selectRepresentativePrice() - 대표 분양가 선택")
    class SelectRepresentativePrice {

        @Test
        @DisplayName("84㎡에 가장 가까운 주택형을 선택한다")
        void selectsClosestTo84() {
            // given
            SubscriptionPrice price80 = createPrice("080.0000T", 40000L, 50);
            SubscriptionPrice price84 = createPrice("084.9543T", 50000L, 100);
            SubscriptionPrice price59 = createPrice("059.9876T", 30000L, 200);

            // when
            SubscriptionPrice result = calculator.selectRepresentativePrice(List.of(price80, price84, price59));

            // then
            assertThat(result.getHouseType()).isEqualTo("084.9543T");
        }

        @Test
        @DisplayName("84㎡ 근접 주택형이 없으면 최대 공급수로 선택한다")
        void selectsMaxSupplyWhenNo84() {
            // given
            SubscriptionPrice price40 = createPrice("040.0000T", 20000L, 50);
            SubscriptionPrice price50 = createPrice("050.0000T", 25000L, 200);

            // when
            SubscriptionPrice result = calculator.selectRepresentativePrice(List.of(price40, price50));

            // then
            assertThat(result.getHouseType()).isEqualTo("050.0000T");
        }

        @Test
        @DisplayName("빈 리스트를 입력하면 null을 반환한다")
        void returnsNullForEmptyList() {
            // when
            SubscriptionPrice result = calculator.selectRepresentativePrice(List.of());

            // then
            assertThat(result).isNull();
        }
    }

    private Subscription createSubscription(String houseManageNo, String address, String source) {
        return Subscription.builder()
                .id(1L)
                .houseManageNo(houseManageNo)
                .address(address)
                .source(source)
                .area("서울")
                .houseName("테스트아파트")
                .build();
    }

    private SubscriptionPrice createPrice(String houseType, Long topAmount, int supplyCount) {
        return SubscriptionPrice.builder()
                .houseType(houseType)
                .topAmount(topAmount)
                .supplyCount(supplyCount)
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
