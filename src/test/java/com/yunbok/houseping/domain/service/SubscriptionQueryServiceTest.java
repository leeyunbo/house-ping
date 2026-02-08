package com.yunbok.houseping.domain.service;

import com.yunbok.houseping.core.service.subscription.SubscriptionQueryService;
import com.yunbok.houseping.core.domain.Subscription;
import com.yunbok.houseping.core.domain.SubscriptionPrice;
import com.yunbok.houseping.core.domain.SubscriptionStatus;
import com.yunbok.houseping.adapter.persistence.SubscriptionPriceQueryAdapter;
import com.yunbok.houseping.adapter.persistence.SubscriptionQueryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@DisplayName("SubscriptionQueryService - 청약 조회 서비스")
@ExtendWith(MockitoExtension.class)
class SubscriptionQueryServiceTest {

    @Mock
    private SubscriptionQueryAdapter subscriptionQueryPort;

    @Mock
    private SubscriptionPriceQueryAdapter subscriptionPriceQueryPort;

    private SubscriptionQueryService service;

    @BeforeEach
    void setUp() {
        service = new SubscriptionQueryService(subscriptionQueryPort, subscriptionPriceQueryPort);
    }

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("ID로 청약 정보를 조회한다")
        void findsSubscriptionById() {
            // given
            Subscription subscription = createSubscription("ApplyHome", "서울", SubscriptionStatus.ACTIVE);
            when(subscriptionQueryPort.findById(1L)).thenReturn(Optional.of(subscription));

            // when
            Optional<Subscription> result = service.findById(1L);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getSource()).isEqualTo("ApplyHome");
        }

        @Test
        @DisplayName("존재하지 않는 ID는 빈 Optional을 반환한다")
        void returnsEmptyWhenNotFound() {
            // given
            when(subscriptionQueryPort.findById(999L)).thenReturn(Optional.empty());

            // when
            Optional<Subscription> result = service.findById(999L);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findActiveAndUpcomingSubscriptions()")
    class FindActiveAndUpcomingSubscriptions {

        @Test
        @DisplayName("지역 필터가 있으면 해당 지역으로 조회한다")
        void filtersbyAreaWhenProvided() {
            // given
            Subscription subscription = createSubscription("ApplyHome", "서울", SubscriptionStatus.ACTIVE);
            when(subscriptionQueryPort.findByAreaContaining("서울"))
                    .thenReturn(List.of(subscription));

            // when
            List<Subscription> result = service.findActiveAndUpcomingSubscriptions("서울");

            // then
            assertThat(result).hasSize(1);
            verify(subscriptionQueryPort).findByAreaContaining("서울");
            verify(subscriptionQueryPort, never()).findBySourceAndAreas(any(), anyList());
        }

        @Test
        @DisplayName("지역 필터가 없으면 기본 조건으로 조회한다")
        void usesDefaultWhenAreaNotProvided() {
            // given
            Subscription subscription = createSubscription("ApplyHome", "서울", SubscriptionStatus.ACTIVE);
            when(subscriptionQueryPort.findBySupportedAreas(List.of("서울", "경기")))
                    .thenReturn(List.of(subscription));

            // when
            List<Subscription> result = service.findActiveAndUpcomingSubscriptions(null);

            // then
            assertThat(result).hasSize(1);
            verify(subscriptionQueryPort).findBySupportedAreas(List.of("서울", "경기"));
        }

        @Test
        @DisplayName("ApplyHome과 LH 소스 모두 포함한다")
        void includesBothApplyHomeAndLH() {
            // given
            Subscription applyHome = createSubscription("ApplyHome", "서울", SubscriptionStatus.ACTIVE);
            Subscription lh = createSubscription("LH", "서울", SubscriptionStatus.ACTIVE);
            when(subscriptionQueryPort.findByAreaContaining("서울"))
                    .thenReturn(List.of(applyHome, lh));

            // when
            List<Subscription> result = service.findActiveAndUpcomingSubscriptions("서울");

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(Subscription::getSource)
                    .containsExactlyInAnyOrder("ApplyHome", "LH");
        }

        @Test
        @DisplayName("서울/경기 지역만 필터링한다")
        void filtersOnlySeoulAndGyeonggi() {
            // given
            Subscription seoul = createSubscription("ApplyHome", "서울특별시", SubscriptionStatus.ACTIVE);
            Subscription busan = createSubscription("ApplyHome", "부산광역시", SubscriptionStatus.ACTIVE);
            when(subscriptionQueryPort.findByAreaContaining("서울"))
                    .thenReturn(List.of(seoul, busan));

            // when
            List<Subscription> result = service.findActiveAndUpcomingSubscriptions("서울");

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getArea()).contains("서울");
        }

        @Test
        @DisplayName("ACTIVE와 UPCOMING 상태만 필터링한다")
        void filtersOnlyActiveAndUpcoming() {
            // given
            Subscription active = createSubscription("ApplyHome", "서울", SubscriptionStatus.ACTIVE);
            Subscription upcoming = createSubscription("ApplyHome", "서울", SubscriptionStatus.UPCOMING);
            Subscription closed = createSubscription("ApplyHome", "서울", SubscriptionStatus.CLOSED);
            when(subscriptionQueryPort.findByAreaContaining("서울"))
                    .thenReturn(List.of(active, upcoming, closed));

            // when
            List<Subscription> result = service.findActiveAndUpcomingSubscriptions("서울");

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(Subscription::getStatus)
                    .containsExactlyInAnyOrder(SubscriptionStatus.ACTIVE, SubscriptionStatus.UPCOMING);
        }

        @Test
        @DisplayName("빈 문자열 지역은 null과 동일하게 처리한다")
        void treatsBlankAreaAsNull() {
            // given
            when(subscriptionQueryPort.findBySupportedAreas(List.of("서울", "경기")))
                    .thenReturn(List.of());

            // when
            service.findActiveAndUpcomingSubscriptions("   ");

            // then
            verify(subscriptionQueryPort).findBySupportedAreas(List.of("서울", "경기"));
        }
    }

    @Nested
    @DisplayName("filterActiveSubscriptions()")
    class FilterActiveSubscriptions {

        @Test
        @DisplayName("ACTIVE 상태만 필터링한다")
        void filtersOnlyActive() {
            // given
            Subscription active = createSubscription("ApplyHome", "서울", SubscriptionStatus.ACTIVE);
            Subscription upcoming = createSubscription("ApplyHome", "서울", SubscriptionStatus.UPCOMING);
            List<Subscription> subscriptions = List.of(active, upcoming);

            // when
            List<Subscription> result = service.filterActiveSubscriptions(subscriptions);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("filterUpcomingSubscriptions()")
    class FilterUpcomingSubscriptions {

        @Test
        @DisplayName("UPCOMING 상태만 필터링한다")
        void filtersOnlyUpcoming() {
            // given
            Subscription active = createSubscription("ApplyHome", "서울", SubscriptionStatus.ACTIVE);
            Subscription upcoming = createSubscription("ApplyHome", "서울", SubscriptionStatus.UPCOMING);
            List<Subscription> subscriptions = List.of(active, upcoming);

            // when
            List<Subscription> result = service.filterUpcomingSubscriptions(subscriptions);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo(SubscriptionStatus.UPCOMING);
        }
    }

    @Nested
    @DisplayName("findPricesByHouseManageNo()")
    class FindPricesByHouseManageNo {

        @Test
        @DisplayName("houseManageNo가 null이면 빈 리스트를 반환한다")
        void returnsEmptyListWhenNull() {
            // when
            List<SubscriptionPrice> result = service.findPricesByHouseManageNo(null);

            // then
            assertThat(result).isEmpty();
            verify(subscriptionPriceQueryPort, never()).findByHouseManageNo(any());
        }

        @Test
        @DisplayName("houseManageNo가 빈 문자열이면 빈 리스트를 반환한다")
        void returnsEmptyListWhenBlank() {
            // when
            List<SubscriptionPrice> result = service.findPricesByHouseManageNo("   ");

            // then
            assertThat(result).isEmpty();
            verify(subscriptionPriceQueryPort, never()).findByHouseManageNo(any());
        }

        @Test
        @DisplayName("유효한 houseManageNo로 가격 정보를 조회한다")
        void findsPricesWithValidHouseManageNo() {
            // given
            SubscriptionPrice price = SubscriptionPrice.builder()
                    .houseManageNo("12345")
                    .houseType("84A")
                    .topAmount(50000L)
                    .build();
            when(subscriptionPriceQueryPort.findByHouseManageNo("12345"))
                    .thenReturn(List.of(price));

            // when
            List<SubscriptionPrice> result = service.findPricesByHouseManageNo("12345");

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getHouseManageNo()).isEqualTo("12345");
        }
    }

    private Subscription createSubscription(String source, String area, SubscriptionStatus status) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = status == SubscriptionStatus.UPCOMING ? today.plusDays(1) : today.minusDays(1);
        LocalDate endDate = status == SubscriptionStatus.CLOSED ? today.minusDays(1) : today.plusDays(5);

        return Subscription.builder()
                .id(1L)
                .source(source)
                .houseName("테스트 아파트")
                .area(area)
                .receiptStartDate(startDate)
                .receiptEndDate(endDate)
                .build();
    }
}
