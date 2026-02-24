package com.yunbok.houseping.core.service.subscription;

import com.yunbok.houseping.core.domain.Subscription;
import com.yunbok.houseping.core.port.SubscriptionPersistencePort;
import com.yunbok.houseping.entity.CompetitionRateEntity;
import com.yunbok.houseping.repository.CompetitionRateRepository;
import com.yunbok.houseping.support.dto.*;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("SubscriptionSearchService - 청약 조회 서비스")
@ExtendWith(MockitoExtension.class)
class SubscriptionSearchServiceTest {

    @Mock
    private SubscriptionPersistencePort subscriptionQueryPort;

    @Mock
    private CompetitionRateRepository competitionRateRepository;

    @Mock
    private PriceBadgeCalculator priceBadgeCalculator;

    private SubscriptionSearchService service;

    @BeforeEach
    void setUp() {
        service = new SubscriptionSearchService(subscriptionQueryPort, competitionRateRepository, priceBadgeCalculator);
    }

    @Nested
    @DisplayName("findById() - ID로 청약 조회")
    class FindById {

        @Test
        @DisplayName("존재하는 청약을 반환한다")
        void returnsSubscription() {
            // given
            Subscription subscription = createSubscription(1L, "서울", "테스트아파트",
                    LocalDate.now().minusDays(1), LocalDate.now().plusDays(5));
            when(subscriptionQueryPort.findById(1L)).thenReturn(Optional.of(subscription));

            // when
            Optional<Subscription> result = service.findById(1L);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getHouseName()).isEqualTo("테스트아파트");
        }

        @Test
        @DisplayName("존재하지 않는 청약은 빈 Optional을 반환한다")
        void returnsEmptyForNonExistent() {
            // given
            when(subscriptionQueryPort.findById(99L)).thenReturn(Optional.empty());

            // when
            Optional<Subscription> result = service.findById(99L);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findActiveAndUpcomingSubscriptions() - 활성/예정 청약 조회")
    class FindActiveAndUpcoming {

        @Test
        @DisplayName("지역 필터로 서울 지역 활성 청약을 조회한다")
        void filtersWithArea() {
            // given
            Subscription active = createSubscription(1L, "서울", "서울아파트",
                    LocalDate.now().minusDays(1), LocalDate.now().plusDays(5));
            when(subscriptionQueryPort.findByAreaContaining("서울")).thenReturn(List.of(active));

            // when
            List<Subscription> result = service.findActiveAndUpcomingSubscriptions("서울");

            // then
            assertThat(result).hasSize(1);
            verify(subscriptionQueryPort).findByAreaContaining("서울");
        }

        @Test
        @DisplayName("지역 미지정 시 서울/경기 전체를 조회한다")
        void queriesAllSupportedAreasWhenNoArea() {
            // given
            Subscription seoul = createSubscription(1L, "서울", "서울아파트",
                    LocalDate.now().minusDays(1), LocalDate.now().plusDays(5));
            Subscription gyeonggi = createSubscription(2L, "경기", "경기아파트",
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(10));
            when(subscriptionQueryPort.findBySupportedAreas(List.of("서울", "경기")))
                    .thenReturn(List.of(seoul, gyeonggi));

            // when
            List<Subscription> result = service.findActiveAndUpcomingSubscriptions(null);

            // then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("빈 결과를 반환한다")
        void returnsEmptyList() {
            // given
            when(subscriptionQueryPort.findBySupportedAreas(any())).thenReturn(List.of());

            // when
            List<Subscription> result = service.findActiveAndUpcomingSubscriptions(null);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("마감된 청약은 제외한다")
        void excludesClosedSubscriptions() {
            // given
            Subscription closed = createSubscription(1L, "서울", "마감아파트",
                    LocalDate.now().minusDays(10), LocalDate.now().minusDays(1));
            when(subscriptionQueryPort.findBySupportedAreas(any())).thenReturn(List.of(closed));

            // when
            List<Subscription> result = service.findActiveAndUpcomingSubscriptions(null);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getAllActiveAndUpcoming() - 모든 활성/예정 카드뷰 조회")
    class GetAllActiveAndUpcoming {

        @Test
        @DisplayName("청약에 가격 배지를 포함한 카드뷰를 반환한다")
        void returnsCardsWithBadge() {
            // given
            Subscription sub = createSubscription(1L, "서울", "테스트아파트",
                    LocalDate.now().minusDays(1), LocalDate.now().plusDays(5));
            when(subscriptionQueryPort.findBySupportedAreas(any())).thenReturn(List.of(sub));
            when(priceBadgeCalculator.computePriceBadge(any())).thenReturn(PriceBadge.CHEAP);

            // when
            List<SubscriptionCardView> result = service.getAllActiveAndUpcoming();

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getPriceBadge()).isEqualTo(PriceBadge.CHEAP);
        }
    }

    @Nested
    @DisplayName("getHomeData() - 홈 페이지 데이터")
    class GetHomeData {

        @Test
        @DisplayName("활성/예정/발표 데이터를 분리하여 반환한다")
        void separatesActiveUpcomingAnnounced() {
            // given
            Subscription active = createSubscription(1L, "서울", "접수중아파트",
                    LocalDate.now().minusDays(1), LocalDate.now().plusDays(5));
            Subscription upcoming = createSubscription(2L, "경기", "예정아파트",
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(10));
            when(subscriptionQueryPort.findBySupportedAreas(any())).thenReturn(List.of(active, upcoming));
            when(priceBadgeCalculator.computePriceBadge(any())).thenReturn(PriceBadge.UNKNOWN);
            when(competitionRateRepository.findDistinctHouseManageNos()).thenReturn(List.of());

            // when
            HomePageResult result = service.getHomeData(null);

            // then
            assertThat(result.getActiveSubscriptions()).hasSize(1);
            assertThat(result.getUpcomingSubscriptions()).hasSize(1);
            assertThat(result.getAreas()).containsExactly("서울", "경기");
        }
    }

    @Nested
    @DisplayName("findAnnouncedSubscriptions() - 발표된 청약 조회")
    class FindAnnouncedSubscriptions {

        @Test
        @DisplayName("경쟁률이 있는 마감 청약을 반환한다")
        void returnsClosedWithCompetitionRates() {
            // given
            Subscription closed = createSubscriptionWithHouseManageNo(1L, "서울", "발표아파트",
                    LocalDate.now().minusDays(5), LocalDate.now().minusDays(1), "H001");
            when(subscriptionQueryPort.findBySupportedAreas(any())).thenReturn(List.of(closed));
            when(competitionRateRepository.findDistinctHouseManageNos()).thenReturn(List.of("H001"));
            when(competitionRateRepository.findByHouseManageNo("H001")).thenReturn(List.of(
                    CompetitionRateEntity.builder()
                            .houseManageNo("H001")
                            .rank(1)
                            .residenceArea("해당지역")
                            .competitionRate(new BigDecimal("5.0"))
                            .build()
            ));

            // when
            List<AnnouncedSubscriptionView> result = service.findAnnouncedSubscriptions(null);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTopRate()).isEqualTo(new BigDecimal("5.0"));
        }

        @Test
        @DisplayName("2주 이상 지난 청약은 제외한다")
        void excludesOldSubscriptions() {
            // given
            Subscription oldClosed = createSubscriptionWithHouseManageNo(1L, "서울", "오래된아파트",
                    LocalDate.now().minusDays(30), LocalDate.now().minusDays(20), "H001");
            when(subscriptionQueryPort.findBySupportedAreas(any())).thenReturn(List.of(oldClosed));
            when(competitionRateRepository.findDistinctHouseManageNos()).thenReturn(List.of("H001"));

            // when
            List<AnnouncedSubscriptionView> result = service.findAnnouncedSubscriptions(null);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findSubscriptionsForWeek() - 주간 청약 조회")
    class FindSubscriptionsForWeek {

        @Test
        @DisplayName("해당 주의 서울/경기 청약을 접수 시작일 순으로 반환한다")
        void returnsWeekSubscriptionsSorted() {
            // given
            LocalDate weekStart = LocalDate.of(2026, 2, 23);
            LocalDate weekEnd = LocalDate.of(2026, 3, 1);
            Subscription sub1 = createSubscription(1L, "서울", "나중아파트",
                    LocalDate.of(2026, 2, 25), LocalDate.of(2026, 2, 27));
            Subscription sub2 = createSubscription(2L, "경기", "먼저아파트",
                    LocalDate.of(2026, 2, 23), LocalDate.of(2026, 2, 25));
            when(subscriptionQueryPort.findByReceiptPeriodOverlapping(weekStart, weekEnd))
                    .thenReturn(List.of(sub1, sub2));

            // when
            List<Subscription> result = service.findSubscriptionsForWeek(weekStart, weekEnd);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getHouseName()).isEqualTo("먼저아파트");
        }
    }

    @Nested
    @DisplayName("findByMonth() - 월별 청약 조회")
    class FindByMonth {

        @Test
        @DisplayName("해당 월의 첫날부터 마지막날까지 청약을 조회한다")
        void queriesMonthRange() {
            // given
            when(subscriptionQueryPort.findByReceiptStartDateBetween(
                    LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28)))
                    .thenReturn(List.of());

            // when
            List<Subscription> result = service.findByMonth(2026, 2);

            // then
            verify(subscriptionQueryPort).findByReceiptStartDateBetween(
                    LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28));
        }
    }

    @Nested
    @DisplayName("findAll() - 전체 청약 조회")
    class FindAll {

        @Test
        @DisplayName("전체 청약 목록을 반환한다")
        void returnsAll() {
            // given
            when(subscriptionQueryPort.findAll()).thenReturn(List.of(
                    createSubscription(1L, "서울", "아파트1", LocalDate.now(), LocalDate.now().plusDays(5)),
                    createSubscription(2L, "경기", "아파트2", LocalDate.now(), LocalDate.now().plusDays(5))
            ));

            // when
            List<Subscription> result = service.findAll();

            // then
            assertThat(result).hasSize(2);
        }
    }

    private Subscription createSubscription(Long id, String area, String houseName,
                                             LocalDate receiptStart, LocalDate receiptEnd) {
        return Subscription.builder()
                .id(id)
                .area(area)
                .houseName(houseName)
                .receiptStartDate(receiptStart)
                .receiptEndDate(receiptEnd)
                .source("ApplyHome")
                .build();
    }

    private Subscription createSubscriptionWithHouseManageNo(Long id, String area, String houseName,
                                                              LocalDate receiptStart, LocalDate receiptEnd,
                                                              String houseManageNo) {
        return Subscription.builder()
                .id(id)
                .area(area)
                .houseName(houseName)
                .receiptStartDate(receiptStart)
                .receiptEndDate(receiptEnd)
                .houseManageNo(houseManageNo)
                .source("ApplyHome")
                .build();
    }
}
