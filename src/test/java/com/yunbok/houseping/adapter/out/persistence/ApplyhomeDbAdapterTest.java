package com.yunbok.houseping.adapter.out.persistence;

import com.yunbok.houseping.adapter.persistence.ApplyhomeDbAdapter;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@DisplayName("ApplyhomeDbAdapter - 청약홈 DB 어댑터")
@ExtendWith(MockitoExtension.class)
class ApplyhomeDbAdapterTest {

    @Mock
    private SubscriptionRepository repository;

    private ApplyhomeDbAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ApplyhomeDbAdapter(repository);
    }

    @Nested
    @DisplayName("fetch() - 청약 정보 조회")
    class Fetch {

        @Test
        @DisplayName("특정 지역과 날짜의 청약 정보를 조회한다")
        void fetchesSubscriptionsByAreaAndDate() {
            // given
            LocalDate targetDate = LocalDate.of(2025, 1, 15);
            SubscriptionEntity entity = createEntity("ApplyHome", "서울");
            when(repository.findByAreaAndReceiptStartDate("서울", targetDate))
                    .thenReturn(List.of(entity));

            // when
            List<SubscriptionInfo> result = adapter.fetch("서울", targetDate);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getHouseName()).isEqualTo("테스트 아파트");
        }

        @Test
        @DisplayName("ApplyHome 소스만 필터링한다")
        void filtersOnlyApplyHomeSource() {
            // given
            LocalDate targetDate = LocalDate.of(2025, 1, 15);
            SubscriptionEntity applyhome = createEntity("ApplyHome", "서울");
            SubscriptionEntity lh = createEntity("LH", "서울");
            when(repository.findByAreaAndReceiptStartDate("서울", targetDate))
                    .thenReturn(List.of(applyhome, lh));

            // when
            List<SubscriptionInfo> result = adapter.fetch("서울", targetDate);

            // then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("데이터가 없으면 빈 리스트를 반환한다")
        void returnsEmptyWhenNoData() {
            // given
            LocalDate targetDate = LocalDate.of(2025, 1, 15);
            when(repository.findByAreaAndReceiptStartDate("서울", targetDate))
                    .thenReturn(List.of());

            // when
            List<SubscriptionInfo> result = adapter.fetch("서울", targetDate);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("조회 실패 시 빈 리스트를 반환한다")
        void returnsEmptyOnException() {
            // given
            LocalDate targetDate = LocalDate.of(2025, 1, 15);
            when(repository.findByAreaAndReceiptStartDate("서울", targetDate))
                    .thenThrow(new RuntimeException("DB 오류"));

            // when
            List<SubscriptionInfo> result = adapter.fetch("서울", targetDate);

            // then
            assertThat(result).isEmpty();
        }
    }

    private SubscriptionEntity createEntity(String source, String area) {
        return SubscriptionEntity.builder()
                .houseManageNo("H001")
                .houseName("테스트 아파트")
                .houseType("APT")
                .area(area)
                .source(source)
                .receiptStartDate(LocalDate.of(2025, 1, 15))
                .receiptEndDate(LocalDate.of(2025, 1, 25))
                .collectedAt(LocalDateTime.now())
                .build();
    }
}
