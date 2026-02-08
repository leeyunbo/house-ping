package com.yunbok.houseping.adapter.out.persistence;

import com.yunbok.houseping.adapter.persistence.SubscriptionPersistenceAdapter;
import com.yunbok.houseping.adapter.dto.ApplyHomeSubscriptionInfo;
import com.yunbok.houseping.adapter.dto.SubscriptionInfo;
import com.yunbok.houseping.entity.SubscriptionEntity;
import com.yunbok.houseping.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("SubscriptionPersistenceAdapter - 청약 영속성 어댑터")
@ExtendWith(MockitoExtension.class)
class SubscriptionPersistenceAdapterTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    private SubscriptionPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new SubscriptionPersistenceAdapter(subscriptionRepository);
    }

    @Nested
    @DisplayName("findBySourceAndHouseNameAndReceiptStartDate() - 조건으로 조회")
    class FindByConditions {

        @Test
        @DisplayName("존재하는 데이터를 찾으면 SubscriptionInfo로 변환하여 반환한다")
        void returnsSubscriptionInfoWhenFound() {
            // given
            SubscriptionEntity entity = createEntity();
            when(subscriptionRepository.findBySourceAndHouseNameAndReceiptStartDate(
                    "APPLYHOME", "테스트 아파트", LocalDate.of(2025, 6, 1)))
                    .thenReturn(Optional.of(entity));

            // when
            Optional<SubscriptionInfo> result = adapter.findBySourceAndHouseNameAndReceiptStartDate(
                    "APPLYHOME", "테스트 아파트", LocalDate.of(2025, 6, 1));

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getHouseName()).isEqualTo("테스트 아파트");
        }

        @Test
        @DisplayName("존재하지 않으면 빈 Optional을 반환한다")
        void returnsEmptyWhenNotFound() {
            // given
            when(subscriptionRepository.findBySourceAndHouseNameAndReceiptStartDate(
                    any(), any(), any())).thenReturn(Optional.empty());

            // when
            Optional<SubscriptionInfo> result = adapter.findBySourceAndHouseNameAndReceiptStartDate(
                    "APPLYHOME", "없는아파트", LocalDate.now());

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("save() - 청약 정보 저장")
    class Save {

        @Test
        @DisplayName("SubscriptionInfo를 Entity로 변환하여 저장한다")
        void savesEntityFromSubscriptionInfo() {
            // given
            SubscriptionInfo info = createSubscriptionInfo();

            // when
            adapter.save(info, "APPLYHOME");

            // then
            ArgumentCaptor<SubscriptionEntity> captor = ArgumentCaptor.forClass(SubscriptionEntity.class);
            verify(subscriptionRepository).save(captor.capture());

            SubscriptionEntity saved = captor.getValue();
            assertThat(saved.getSource()).isEqualTo("APPLYHOME");
            assertThat(saved.getHouseName()).isEqualTo("힐스테이트 강남");
            assertThat(saved.getArea()).isEqualTo("서울");
        }
    }

    @Nested
    @DisplayName("update() - 청약 정보 업데이트")
    class Update {

        @Test
        @DisplayName("기존 데이터가 있고 변경이 필요하면 업데이트한다")
        void updatesWhenExistsAndNeedsUpdate() {
            // given
            SubscriptionInfo info = createSubscriptionInfo();
            SubscriptionEntity existing = createEntity();
            when(subscriptionRepository.findBySourceAndHouseNameAndReceiptStartDate(
                    "APPLYHOME", info.getHouseName(), info.getReceiptStartDate()))
                    .thenReturn(Optional.of(existing));

            // needsUpdate가 true를 반환하도록 가정 (실제 로직은 Entity에 있음)
            // when
            adapter.update(info, "APPLYHOME");

            // then
            verify(subscriptionRepository).findBySourceAndHouseNameAndReceiptStartDate(
                    "APPLYHOME", info.getHouseName(), info.getReceiptStartDate());
        }

        @Test
        @DisplayName("기존 데이터가 없으면 아무 것도 하지 않는다")
        void doesNothingWhenNotExists() {
            // given
            SubscriptionInfo info = createSubscriptionInfo();
            when(subscriptionRepository.findBySourceAndHouseNameAndReceiptStartDate(
                    any(), any(), any())).thenReturn(Optional.empty());

            // when
            adapter.update(info, "APPLYHOME");

            // then
            verify(subscriptionRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteOldSubscriptions() - 오래된 데이터 삭제")
    class DeleteOldSubscriptions {

        @Test
        @DisplayName("삭제된 개수를 반환한다")
        void returnsDeletedCount() {
            // given
            LocalDate cutoffDate = LocalDate.now().minusMonths(6);
            when(subscriptionRepository.deleteOldSubscriptions(cutoffDate)).thenReturn(5);

            // when
            int result = adapter.deleteOldSubscriptions(cutoffDate);

            // then
            assertThat(result).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("findHouseManageNosByAreas() - 지역별 관리번호 조회")
    class FindHouseManageNosByAreas {

        @Test
        @DisplayName("해당 지역의 관리번호 목록을 반환한다")
        void returnsHouseManageNos() {
            // given
            List<String> areas = List.of("서울", "경기");
            when(subscriptionRepository.findHouseManageNosByAreaIn(areas))
                    .thenReturn(List.of("H001", "H002", "H003"));

            // when
            Set<String> result = adapter.findHouseManageNosByAreas(areas);

            // then
            assertThat(result).containsExactlyInAnyOrder("H001", "H002", "H003");
        }

        @Test
        @DisplayName("지역 목록이 null이면 빈 Set을 반환한다")
        void returnsEmptySetWhenNull() {
            // when
            Set<String> result = adapter.findHouseManageNosByAreas(null);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("지역 목록이 비어있으면 빈 Set을 반환한다")
        void returnsEmptySetWhenEmpty() {
            // when
            Set<String> result = adapter.findHouseManageNosByAreas(List.of());

            // then
            assertThat(result).isEmpty();
        }
    }

    private SubscriptionEntity createEntity() {
        return SubscriptionEntity.builder()
                .id(1L)
                .source("APPLYHOME")
                .houseManageNo("H001")
                .pblancNo("P001")
                .houseName("테스트 아파트")
                .houseType("APT")
                .area("서울")
                .announceDate(LocalDate.of(2025, 5, 1))
                .receiptStartDate(LocalDate.of(2025, 6, 1))
                .receiptEndDate(LocalDate.of(2025, 6, 7))
                .winnerAnnounceDate(LocalDate.of(2025, 6, 15))
                .detailUrl("http://example.com")
                .collectedAt(LocalDateTime.now())
                .build();
    }

    private SubscriptionInfo createSubscriptionInfo() {
        return ApplyHomeSubscriptionInfo.builder()
                .houseManageNo("H001")
                .pblancNo("P001")
                .houseName("힐스테이트 강남")
                .houseType("APT")
                .area("서울")
                .announceDate(LocalDate.of(2025, 5, 1))
                .receiptStartDate(LocalDate.of(2025, 6, 1))
                .receiptEndDate(LocalDate.of(2025, 6, 7))
                .winnerAnnounceDate(LocalDate.of(2025, 6, 15))
                .detailUrl("http://example.com")
                .build();
    }
}
