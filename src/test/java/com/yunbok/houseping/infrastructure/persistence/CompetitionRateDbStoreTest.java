package com.yunbok.houseping.infrastructure.persistence;

import com.yunbok.houseping.core.domain.CompetitionRate;
import com.yunbok.houseping.entity.CompetitionRateEntity;
import com.yunbok.houseping.repository.CompetitionRateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("CompetitionRateDbStore - 경쟁률 DB 어댑터")
@ExtendWith(MockitoExtension.class)
class CompetitionRateDbStoreTest {

    @Mock
    private CompetitionRateRepository repository;

    @Captor
    private ArgumentCaptor<CompetitionRateEntity> entityCaptor;

    @Captor
    private ArgumentCaptor<List<CompetitionRateEntity>> entityListCaptor;

    private CompetitionRateDbStore adapter;

    @BeforeEach
    void setUp() {
        adapter = new CompetitionRateDbStore(repository);
    }

    @Nested
    @DisplayName("save() - 단일 경쟁률 저장")
    class Save {

        @Test
        @DisplayName("경쟁률을 엔티티로 변환하여 저장한다")
        void savesCompetitionRate() {
            // given
            CompetitionRate rate = createDomainRate("H001", "P001");

            // when
            adapter.save(rate);

            // then
            verify(repository).save(entityCaptor.capture());
            CompetitionRateEntity saved = entityCaptor.getValue();
            assertThat(saved.getHouseManageNo()).isEqualTo("H001");
            assertThat(saved.getPblancNo()).isEqualTo("P001");
        }

        @Test
        @DisplayName("저장 시 collectedAt을 현재 시간으로 설정한다")
        void setsCollectedAtToNow() {
            // given
            CompetitionRate rate = createDomainRate("H001", "P001");
            LocalDateTime before = LocalDateTime.now();

            // when
            adapter.save(rate);

            // then
            verify(repository).save(entityCaptor.capture());
            CompetitionRateEntity saved = entityCaptor.getValue();
            assertThat(saved.getCollectedAt()).isAfterOrEqualTo(before);
        }
    }

    @Nested
    @DisplayName("saveAll() - 다건 경쟁률 저장")
    class SaveAll {

        @Test
        @DisplayName("여러 경쟁률을 엔티티 목록으로 변환하여 저장한다")
        void savesAllCompetitionRates() {
            // given
            List<CompetitionRate> rates = List.of(
                    createDomainRate("H001", "P001"),
                    createDomainRate("H002", "P002")
            );

            // when
            adapter.saveAll(rates);

            // then
            verify(repository).saveAll(entityListCaptor.capture());
            List<CompetitionRateEntity> saved = entityListCaptor.getValue();
            assertThat(saved).hasSize(2);
        }

        @Test
        @DisplayName("빈 리스트를 저장해도 예외가 발생하지 않는다")
        void savesEmptyList() {
            // given
            List<CompetitionRate> rates = List.of();

            // when
            adapter.saveAll(rates);

            // then
            verify(repository).saveAll(entityListCaptor.capture());
            assertThat(entityListCaptor.getValue()).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByHouseManageNoAndPblancNo() - 경쟁률 조회")
    class FindByHouseManageNoAndPblancNo {

        @Test
        @DisplayName("존재하는 경쟁률을 조회한다")
        void findsExistingRates() {
            // given
            CompetitionRateEntity entity = createEntity("H001", "P001");
            when(repository.findByHouseManageNoAndPblancNo("H001", "P001"))
                    .thenReturn(List.of(entity));

            // when
            List<CompetitionRate> result = adapter.findByHouseManageNoAndPblancNo("H001", "P001");

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getHouseManageNo()).isEqualTo("H001");
        }

        @Test
        @DisplayName("존재하지 않으면 빈 리스트를 반환한다")
        void returnsEmptyWhenNotFound() {
            // given
            when(repository.findByHouseManageNoAndPblancNo("H999", "P999"))
                    .thenReturn(List.of());

            // when
            List<CompetitionRate> result = adapter.findByHouseManageNoAndPblancNo("H999", "P999");

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByHouseManageNoAndPblancNo() - 존재 여부 확인")
    class ExistsByHouseManageNoAndPblancNo {

        @Test
        @DisplayName("존재하면 true를 반환한다")
        void returnsTrueWhenExists() {
            // given
            when(repository.existsByHouseManageNoAndPblancNo("H001", "P001"))
                    .thenReturn(true);

            // when
            boolean result = adapter.existsByHouseManageNoAndPblancNo("H001", "P001");

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("존재하지 않으면 false를 반환한다")
        void returnsFalseWhenNotExists() {
            // given
            when(repository.existsByHouseManageNoAndPblancNo("H999", "P999"))
                    .thenReturn(false);

            // when
            boolean result = adapter.existsByHouseManageNoAndPblancNo("H999", "P999");

            // then
            assertThat(result).isFalse();
        }
    }

    private CompetitionRate createDomainRate(String houseManageNo, String pblancNo) {
        return CompetitionRate.builder()
                .houseManageNo(houseManageNo)
                .pblancNo(pblancNo)
                .houseType("084T")
                .supplyCount(100)
                .requestCount(500)
                .competitionRate(new BigDecimal("5.0"))
                .residenceArea("해당지역")
                .rank(1)
                .build();
    }

    private CompetitionRateEntity createEntity(String houseManageNo, String pblancNo) {
        return CompetitionRateEntity.builder()
                .houseManageNo(houseManageNo)
                .pblancNo(pblancNo)
                .houseType("084T")
                .supplyCount(100)
                .requestCount(500)
                .competitionRate(new BigDecimal("5.0"))
                .residenceArea("해당지역")
                .rank(1)
                .collectedAt(LocalDateTime.now())
                .build();
    }
}
