package com.yunbok.houseping.adapter.out.persistence;

import com.yunbok.houseping.adapter.persistence.LhDbAdapter;
import com.yunbok.houseping.adapter.dto.SubscriptionInfo;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("LhDbAdapter - LH DB 어댑터 (Fallback용)")
@ExtendWith(MockitoExtension.class)
class LhDbAdapterTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    private LhDbAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new LhDbAdapter(subscriptionRepository);
    }

    @Nested
    @DisplayName("fetch() - 데이터 조회")
    class Fetch {

        @Test
        @DisplayName("LH_API 소스의 데이터만 필터링해서 반환한다")
        void filtersLhSourceOnly() {
            // given
            LocalDate targetDate = LocalDate.now();
            String areaName = "서울";

            List<SubscriptionEntity> entities = List.of(
                    createEntity("LH_API", "LH 행복주택"),
                    createEntity("APPLYHOME_API", "청약Home 아파트"),
                    createEntity("LH_API", "LH 신혼희망타운")
            );

            when(subscriptionRepository.findByAreaAndReceiptStartDateGreaterThanEqual(eq(areaName), any()))
                    .thenReturn(entities);

            // when
            List<SubscriptionInfo> result = adapter.fetch(areaName, targetDate);

            // then - LH_API 소스만 2건
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(s -> s.getHouseName().contains("LH"));
        }

        @Test
        @DisplayName("해당 지역의 금일 이후 접수 시작 청약만 조회한다")
        void queriesUpcomingSubscriptions() {
            // given
            LocalDate targetDate = LocalDate.now();
            String areaName = "경기";

            when(subscriptionRepository.findByAreaAndReceiptStartDateGreaterThanEqual(eq(areaName), any()))
                    .thenReturn(List.of());

            // when
            adapter.fetch(areaName, targetDate);

            // then
            verify(subscriptionRepository).findByAreaAndReceiptStartDateGreaterThanEqual(eq(areaName), any());
        }

        @Test
        @DisplayName("데이터가 없으면 빈 리스트를 반환한다")
        void returnsEmptyListWhenNoData() {
            // given
            LocalDate targetDate = LocalDate.now();

            when(subscriptionRepository.findByAreaAndReceiptStartDateGreaterThanEqual(any(), any()))
                    .thenReturn(List.of());

            // when
            List<SubscriptionInfo> result = adapter.fetch("서울", targetDate);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("DB 조회 실패 시 빈 리스트를 반환한다")
        void returnsEmptyListOnDbError() {
            // given
            LocalDate targetDate = LocalDate.now();

            when(subscriptionRepository.findByAreaAndReceiptStartDateGreaterThanEqual(any(), any()))
                    .thenThrow(new RuntimeException("DB 연결 실패"));

            // when
            List<SubscriptionInfo> result = adapter.fetch("서울", targetDate);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Entity -> LhSubscriptionInfo 변환")
    class EntityMapping {

        @Test
        @DisplayName("LH 전용 필드만 매핑된다")
        void mapsLhSpecificFields() {
            // given
            LocalDate targetDate = LocalDate.now();
            LocalDate announceDate = LocalDate.of(2025, 1, 10);
            LocalDate receiptEnd = LocalDate.of(2025, 1, 25);

            SubscriptionEntity entity = SubscriptionEntity.builder()
                    .source("LH_API")
                    .houseName("LH 행복주택 강남")
                    .houseType("LH 분양주택")
                    .area("서울")
                    .announceDate(announceDate)
                    .receiptEndDate(receiptEnd)
                    .detailUrl("https://lh.or.kr/detail")
                    .receiptStartDate(LocalDate.now().plusDays(1))
                    .build();

            when(subscriptionRepository.findByAreaAndReceiptStartDateGreaterThanEqual(any(), any()))
                    .thenReturn(List.of(entity));

            // when
            List<SubscriptionInfo> result = adapter.fetch("서울", targetDate);

            // then
            assertThat(result).hasSize(1);
            SubscriptionInfo info = result.get(0);
            assertThat(info.getHouseName()).isEqualTo("LH 행복주택 강남");
            assertThat(info.getHouseType()).isEqualTo("LH 분양주택");
            assertThat(info.getArea()).isEqualTo("서울");
            assertThat(info.getAnnounceDate()).isEqualTo(announceDate);
            assertThat(info.getReceiptEndDate()).isEqualTo(receiptEnd);
            assertThat(info.getDetailUrl()).isEqualTo("https://lh.or.kr/detail");
        }
    }

    private SubscriptionEntity createEntity(String source, String houseName) {
        return SubscriptionEntity.builder()
                .source(source)
                .houseName(houseName)
                .houseType("LH 주택")
                .area("서울")
                .receiptStartDate(LocalDate.now().plusDays(1))
                .build();
    }
}
