package com.yunbok.houseping.adapter.in.web.admin;

import com.yunbok.houseping.infrastructure.persistence.CompetitionRateEntity;
import com.yunbok.houseping.infrastructure.persistence.CompetitionRateRepository;
import com.yunbok.houseping.infrastructure.persistence.SubscriptionEntity;
import com.yunbok.houseping.infrastructure.persistence.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.querydsl.core.types.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("DashboardQueryService - 대시보드 통계 조회")
@ExtendWith(MockitoExtension.class)
class DashboardQueryServiceTest {

    @Mock
    private CompetitionRateRepository competitionRateRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    private DashboardQueryService service;

    @BeforeEach
    void setUp() {
        service = new DashboardQueryService(competitionRateRepository, subscriptionRepository);
    }

    @Nested
    @DisplayName("getStatistics() - 통계 조회")
    class GetStatistics {

        @Test
        @DisplayName("경쟁률 데이터가 없으면 빈 통계를 반환한다")
        void returnsEmptyStatisticsWhenNoData() {
            // given
            when(competitionRateRepository.findAll()).thenReturn(List.of());

            // when
            DashboardStatisticsDto result = service.getStatistics();

            // then
            assertThat(result.summary().totalCount()).isEqualTo(0);
            assertThat(result.summary().avgRate()).isNull();
            assertThat(result.summary().maxRate()).isNull();
            assertThat(result.summary().minRate()).isNull();
            assertThat(result.areaYearlyTrend().years()).isEmpty();
            assertThat(result.byResidenceArea()).isEmpty();
            assertThat(result.byHouseType()).isEmpty();
        }

        @Test
        @DisplayName("경쟁률 데이터가 있으면 통계를 계산하여 반환한다")
        void returnsCalculatedStatistics() {
            // given
            List<CompetitionRateEntity> rates = List.of(
                    createRateEntity("H001", "084.9543T", "해당지역", new BigDecimal("15.5"), 100),
                    createRateEntity("H001", "084.9543T", "기타지역", new BigDecimal("25.0"), 80),
                    createRateEntity("H002", "059.9721A", "해당지역", new BigDecimal("8.0"), 50)
            );
            when(competitionRateRepository.findAll()).thenReturn(rates);

            List<SubscriptionEntity> subscriptions = List.of(
                    createSubscriptionEntity("H001", "서울", 2025),
                    createSubscriptionEntity("H002", "경기", 2025)
            );
            when(subscriptionRepository.findAll(any(Predicate.class))).thenReturn(subscriptions);

            // when
            DashboardStatisticsDto result = service.getStatistics();

            // then
            assertThat(result.summary().totalCount()).isEqualTo(3);
            assertThat(result.byResidenceArea()).isNotEmpty();
        }

        @Test
        @DisplayName("소규모 세대(20세대 미만)는 유효하지 않은 데이터로 처리한다")
        void excludesSmallSupplyFromValidRates() {
            // given
            List<CompetitionRateEntity> rates = List.of(
                    createRateEntity("H001", "084.9543T", "해당지역", new BigDecimal("100.0"), 10), // 10세대 - 제외
                    createRateEntity("H002", "084.9543T", "해당지역", new BigDecimal("15.0"), 50)  // 50세대 - 포함
            );
            when(competitionRateRepository.findAll()).thenReturn(rates);

            List<SubscriptionEntity> subscriptions = List.of(
                    createSubscriptionEntity("H001", "서울", 2025),
                    createSubscriptionEntity("H002", "서울", 2025)
            );
            when(subscriptionRepository.findAll(any(Predicate.class))).thenReturn(subscriptions);

            // when
            DashboardStatisticsDto result = service.getStatistics();

            // then
            // Summary의 avgRate는 유효한 데이터(50세대)만 포함
            assertThat(result.summary().totalCount()).isEqualTo(2); // 전체 개수
        }

        @Test
        @DisplayName("경쟁률이 0 이하인 데이터는 제외한다")
        void excludesZeroOrNegativeRates() {
            // given
            List<CompetitionRateEntity> rates = List.of(
                    createRateEntity("H001", "084.9543T", "해당지역", BigDecimal.ZERO, 50),
                    createRateEntity("H002", "084.9543T", "해당지역", new BigDecimal("-1.0"), 50),
                    createRateEntity("H003", "084.9543T", "해당지역", new BigDecimal("10.0"), 50)
            );
            when(competitionRateRepository.findAll()).thenReturn(rates);

            List<SubscriptionEntity> subscriptions = List.of(
                    createSubscriptionEntity("H001", "서울", 2025),
                    createSubscriptionEntity("H002", "서울", 2025),
                    createSubscriptionEntity("H003", "서울", 2025)
            );
            when(subscriptionRepository.findAll(any(Predicate.class))).thenReturn(subscriptions);

            // when
            DashboardStatisticsDto result = service.getStatistics();

            // then
            assertThat(result.summary().totalCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("거주지역별 통계를 계산한다")
        void calculatesResidenceAreaStats() {
            // given
            List<CompetitionRateEntity> rates = List.of(
                    createRateEntity("H001", "084T", "해당지역", new BigDecimal("10.0"), 50),
                    createRateEntity("H001", "084T", "해당지역", new BigDecimal("20.0"), 50),
                    createRateEntity("H002", "084T", "기타지역", new BigDecimal("5.0"), 50)
            );
            when(competitionRateRepository.findAll()).thenReturn(rates);
            when(subscriptionRepository.findAll(any(Predicate.class))).thenReturn(List.of());

            // when
            DashboardStatisticsDto result = service.getStatistics();

            // then
            assertThat(result.byResidenceArea()).hasSize(2);
        }

        @Test
        @DisplayName("주택형별 통계를 계산한다")
        void calculatesHouseTypeStats() {
            // given
            List<CompetitionRateEntity> rates = List.of(
                    createRateEntity("H001", "084.9543T", "해당지역", new BigDecimal("10.0"), 50),
                    createRateEntity("H002", "059.9721A", "해당지역", new BigDecimal("20.0"), 50)
            );
            when(competitionRateRepository.findAll()).thenReturn(rates);
            when(subscriptionRepository.findAll(any(Predicate.class))).thenReturn(List.of());

            // when
            DashboardStatisticsDto result = service.getStatistics();

            // then
            assertThat(result.byHouseType()).hasSize(2);
        }

        @Test
        @DisplayName("경쟁률 분포를 계산한다")
        void calculatesRateDistribution() {
            // given
            List<CompetitionRateEntity> rates = List.of(
                    createRateEntity("H001", "084T", "해당지역", new BigDecimal("3.0"), 50),   // under5
                    createRateEntity("H002", "084T", "해당지역", new BigDecimal("7.0"), 50),   // 5-10
                    createRateEntity("H003", "084T", "해당지역", new BigDecimal("15.0"), 50),  // 10-20
                    createRateEntity("H004", "084T", "해당지역", new BigDecimal("30.0"), 50),  // 20-50
                    createRateEntity("H005", "084T", "해당지역", new BigDecimal("60.0"), 50)   // over50
            );
            when(competitionRateRepository.findAll()).thenReturn(rates);
            when(subscriptionRepository.findAll(any(Predicate.class))).thenReturn(List.of());

            // when
            DashboardStatisticsDto result = service.getStatistics();

            // then
            DashboardStatisticsDto.RateDistribution dist = result.distribution();
            assertThat(dist.under5()).isEqualTo(1);
            assertThat(dist.from5to10()).isEqualTo(1);
            assertThat(dist.from10to20()).isEqualTo(1);
            assertThat(dist.from20to50()).isEqualTo(1);
            assertThat(dist.over50()).isEqualTo(1);
        }
    }

    private CompetitionRateEntity createRateEntity(String houseManageNo, String houseType,
                                                    String residenceArea, BigDecimal rate, int supplyCount) {
        return CompetitionRateEntity.builder()
                .houseManageNo(houseManageNo)
                .pblancNo("P001")
                .houseType(houseType)
                .residenceArea(residenceArea)
                .competitionRate(rate)
                .supplyCount(supplyCount)
                .requestCount((int) (supplyCount * rate.doubleValue()))
                .rank(1)
                .collectedAt(LocalDateTime.now())
                .build();
    }

    private SubscriptionEntity createSubscriptionEntity(String houseManageNo, String area, int year) {
        return SubscriptionEntity.builder()
                .houseManageNo(houseManageNo)
                .houseName("테스트 아파트")
                .area(area)
                .receiptStartDate(LocalDate.of(year, 6, 1))
                .winnerAnnounceDate(LocalDate.of(year, 6, 15))
                .collectedAt(LocalDateTime.now())
                .build();
    }
}
