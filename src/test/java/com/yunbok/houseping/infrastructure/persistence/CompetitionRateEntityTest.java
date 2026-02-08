package com.yunbok.houseping.infrastructure.persistence;

import com.yunbok.houseping.entity.CompetitionRateEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CompetitionRateEntity - 경쟁률 엔티티")
class CompetitionRateEntityTest {

    @Nested
    @DisplayName("빌더 패턴")
    class Builder {

        @Test
        @DisplayName("빌더로 엔티티를 생성할 수 있다")
        void buildsEntity() {
            // when
            CompetitionRateEntity entity = CompetitionRateEntity.builder()
                    .houseManageNo("H001")
                    .pblancNo("P001")
                    .houseType("084T")
                    .supplyCount(100)
                    .requestCount(500)
                    .competitionRate(new BigDecimal("5.0"))
                    .residenceArea("해당지역")
                    .rank(1)
                    .collectedAt(LocalDateTime.now())
                    .build();

            // then
            assertThat(entity.getHouseManageNo()).isEqualTo("H001");
            assertThat(entity.getPblancNo()).isEqualTo("P001");
            assertThat(entity.getHouseType()).isEqualTo("084T");
            assertThat(entity.getSupplyCount()).isEqualTo(100);
            assertThat(entity.getRequestCount()).isEqualTo(500);
            assertThat(entity.getCompetitionRate()).isEqualByComparingTo("5.0");
            assertThat(entity.getResidenceArea()).isEqualTo("해당지역");
            assertThat(entity.getRank()).isEqualTo(1);
        }

        @Test
        @DisplayName("null 값을 허용한다")
        void allowsNullValues() {
            // when
            CompetitionRateEntity entity = CompetitionRateEntity.builder()
                    .houseManageNo("H001")
                    .pblancNo("P001")
                    .houseType("084T")
                    .supplyCount(null)
                    .requestCount(null)
                    .competitionRate(null)
                    .residenceArea(null)
                    .rank(null)
                    .collectedAt(LocalDateTime.now())
                    .build();

            // then
            assertThat(entity.getSupplyCount()).isNull();
            assertThat(entity.getRequestCount()).isNull();
            assertThat(entity.getCompetitionRate()).isNull();
        }
    }

    @Nested
    @DisplayName("게터")
    class Getters {

        @Test
        @DisplayName("모든 필드에 대한 게터가 동작한다")
        void gettersWork() {
            // given
            LocalDateTime collectedAt = LocalDateTime.of(2025, 1, 15, 10, 0);

            CompetitionRateEntity entity = CompetitionRateEntity.builder()
                    .houseManageNo("H001")
                    .pblancNo("P001")
                    .houseType("084.9543T")
                    .supplyCount(100)
                    .requestCount(1500)
                    .competitionRate(new BigDecimal("15.00"))
                    .residenceArea("기타지역")
                    .rank(2)
                    .collectedAt(collectedAt)
                    .build();

            // then
            assertThat(entity.getHouseManageNo()).isEqualTo("H001");
            assertThat(entity.getPblancNo()).isEqualTo("P001");
            assertThat(entity.getHouseType()).isEqualTo("084.9543T");
            assertThat(entity.getSupplyCount()).isEqualTo(100);
            assertThat(entity.getRequestCount()).isEqualTo(1500);
            assertThat(entity.getCompetitionRate()).isEqualByComparingTo("15.00");
            assertThat(entity.getResidenceArea()).isEqualTo("기타지역");
            assertThat(entity.getRank()).isEqualTo(2);
            assertThat(entity.getCollectedAt()).isEqualTo(collectedAt);
        }
    }
}
