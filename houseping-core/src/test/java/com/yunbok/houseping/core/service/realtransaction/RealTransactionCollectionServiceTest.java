package com.yunbok.houseping.core.service.realtransaction;

import com.yunbok.houseping.core.port.RealTransactionFetchPort;
import com.yunbok.houseping.core.service.region.RegionCodeService;
import com.yunbok.houseping.entity.SubscriptionEntity;
import com.yunbok.houseping.repository.SubscriptionRepository;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("RealTransactionCollectionService - 실거래가 수집 서비스")
@ExtendWith(MockitoExtension.class)
class RealTransactionCollectionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private RegionCodeService regionCodeService;

    @Mock
    private RealTransactionFetchPort realTransactionFetchPort;

    private RealTransactionCollectionService service;

    @BeforeEach
    void setUp() {
        service = new RealTransactionCollectionService(subscriptionRepository, regionCodeService, realTransactionFetchPort);
    }

    @Nested
    @DisplayName("collectRealTransactions() - 실거래가 수집")
    class CollectRealTransactions {

        @Test
        @DisplayName("ApplyHome 활성 청약의 실거래가를 수집한다")
        void collectsForActiveApplyHomeSubscriptions() {
            // given
            SubscriptionEntity active = createEntity("ApplyHome", "서울시 강남구 역삼동",
                    LocalDate.now(), LocalDate.now().plusDays(5));
            when(subscriptionRepository.findAll()).thenReturn(List.of(active));
            when(regionCodeService.findLawdCdByAddress("서울시 강남구 역삼동"))
                    .thenReturn(Optional.of("11680"));
            when(realTransactionFetchPort.fetchAndCacheRecentTransactions("11680", 6))
                    .thenReturn(List.of());

            // when
            service.collectRealTransactions();

            // then
            verify(realTransactionFetchPort).fetchAndCacheRecentTransactions("11680", 6);
        }

        @Test
        @DisplayName("LH 청약은 제외한다")
        void excludesLhSubscriptions() {
            // given
            SubscriptionEntity lh = createEntity("LH", "서울시 강남구",
                    LocalDate.now(), LocalDate.now().plusDays(5));
            when(subscriptionRepository.findAll()).thenReturn(List.of(lh));

            // when
            service.collectRealTransactions();

            // then
            verify(regionCodeService, never()).findLawdCdByAddress(anyString());
            verify(realTransactionFetchPort, never()).fetchAndCacheRecentTransactions(anyString(), anyInt());
        }

        @Test
        @DisplayName("만료된 청약은 제외한다")
        void excludesExpiredSubscriptions() {
            // given
            SubscriptionEntity expired = createEntity("ApplyHome", "서울시 강남구",
                    LocalDate.now().minusDays(10), LocalDate.now().minusDays(5));
            when(subscriptionRepository.findAll()).thenReturn(List.of(expired));

            // when
            service.collectRealTransactions();

            // then
            verify(regionCodeService, never()).findLawdCdByAddress(anyString());
        }

        @Test
        @DisplayName("lawdCd 중복을 제거하여 1회만 호출한다")
        void deduplicatesLawdCodes() {
            // given
            SubscriptionEntity sub1 = createEntity("ApplyHome", "서울시 강남구 역삼동",
                    LocalDate.now(), LocalDate.now().plusDays(5));
            SubscriptionEntity sub2 = createEntity("ApplyHome", "서울시 강남구 삼성동",
                    LocalDate.now(), LocalDate.now().plusDays(5));
            when(subscriptionRepository.findAll()).thenReturn(List.of(sub1, sub2));
            when(regionCodeService.findLawdCdByAddress("서울시 강남구 역삼동")).thenReturn(Optional.of("11680"));
            when(regionCodeService.findLawdCdByAddress("서울시 강남구 삼성동")).thenReturn(Optional.of("11680")); // 같은 코드
            when(realTransactionFetchPort.fetchAndCacheRecentTransactions(anyString(), eq(6)))
                    .thenReturn(List.of());

            // when
            service.collectRealTransactions();

            // then — 중복 제거되어 1회만 호출
            verify(realTransactionFetchPort, times(1)).fetchAndCacheRecentTransactions("11680", 6);
        }

        @Test
        @DisplayName("API 실패 시 다음 지역 수집을 계속한다")
        void continuesOnApiFail() {
            // given
            SubscriptionEntity sub1 = createEntity("ApplyHome", "서울시 강남구 역삼동",
                    LocalDate.now(), LocalDate.now().plusDays(5));
            SubscriptionEntity sub2 = createEntity("ApplyHome", "경기도 수원시 장안구",
                    LocalDate.now(), LocalDate.now().plusDays(5));
            when(subscriptionRepository.findAll()).thenReturn(List.of(sub1, sub2));
            when(regionCodeService.findLawdCdByAddress("서울시 강남구 역삼동")).thenReturn(Optional.of("11680"));
            when(regionCodeService.findLawdCdByAddress("경기도 수원시 장안구")).thenReturn(Optional.of("41111"));
            when(realTransactionFetchPort.fetchAndCacheRecentTransactions("11680", 6))
                    .thenThrow(new RuntimeException("API 오류"));
            when(realTransactionFetchPort.fetchAndCacheRecentTransactions("41111", 6))
                    .thenReturn(List.of());

            // when
            service.collectRealTransactions();

            // then — 첫 번째 실패에도 두 번째가 호출됨
            verify(realTransactionFetchPort).fetchAndCacheRecentTransactions("11680", 6);
            verify(realTransactionFetchPort).fetchAndCacheRecentTransactions("41111", 6);
        }
    }

    private SubscriptionEntity createEntity(String source, String address,
                                             LocalDate receiptStart, LocalDate receiptEnd) {
        return SubscriptionEntity.builder()
                .id(1L)
                .source(source)
                .houseName("테스트아파트")
                .area("서울")
                .address(address)
                .receiptStartDate(receiptStart)
                .receiptEndDate(receiptEnd)
                .build();
    }
}
