package com.yunbok.houseping.infrastructure.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RegionCodeRepository - 법정동코드 Repository")
@DataJpaTest
@ActiveProfiles("test")
class RegionCodeRepositoryTest {

    @Autowired
    private RegionCodeRepository regionCodeRepository;

    @Nested
    @DisplayName("findByLawdCd() - 법정동코드로 조회")
    class FindByLawdCd {

        @Test
        @DisplayName("법정동코드로 지역을 찾는다")
        void findsRegionByLawdCd() {
            // given
            RegionCodeEntity entity = createAndSaveRegionCode("서울특별시", "강남구", "11680");

            // when
            Optional<RegionCodeEntity> result = regionCodeRepository.findByLawdCd("11680");

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getSigunguName()).isEqualTo("강남구");
        }

        @Test
        @DisplayName("존재하지 않는 코드는 빈 Optional을 반환한다")
        void returnsEmptyForNonexistentCode() {
            // when
            Optional<RegionCodeEntity> result = regionCodeRepository.findByLawdCd("99999");

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findBySigunguNameContaining() - 시군구명 부분 일치 조회")
    class FindBySigunguNameContaining {

        @Test
        @DisplayName("시군구명 부분 일치로 지역을 찾는다")
        void findsRegionsByPartialSigunguName() {
            // given
            createAndSaveRegionCode("서울특별시", "강남구", "11680");
            createAndSaveRegionCode("서울특별시", "강동구", "11740");
            createAndSaveRegionCode("서울특별시", "송파구", "11710");

            // when
            List<RegionCodeEntity> result = regionCodeRepository.findBySigunguNameContaining("강");

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting("sigunguName")
                    .containsExactlyInAnyOrder("강남구", "강동구");
        }
    }

    @Nested
    @DisplayName("findBySidoNameAndSigunguName() - 시도명 + 시군구명 조회")
    class FindBySidoNameAndSigunguName {

        @Test
        @DisplayName("시도명과 시군구명으로 지역을 찾는다")
        void findsRegionBySidoAndSigungu() {
            // given
            createAndSaveRegionCode("서울특별시", "강남구", "11680");
            createAndSaveRegionCode("경기도", "성남시", "41131");

            // when
            Optional<RegionCodeEntity> result = regionCodeRepository
                    .findBySidoNameAndSigunguName("서울특별시", "강남구");

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getLawdCd()).isEqualTo("11680");
        }
    }

    @Nested
    @DisplayName("findBySidoName() - 시도명으로 목록 조회")
    class FindBySidoName {

        @Test
        @DisplayName("시도명으로 해당 지역 목록을 찾는다")
        void findsRegionsBySidoName() {
            // given
            createAndSaveRegionCode("서울특별시", "강남구", "11680");
            createAndSaveRegionCode("서울특별시", "송파구", "11710");
            createAndSaveRegionCode("경기도", "수원시", "41111");

            // when
            List<RegionCodeEntity> result = regionCodeRepository.findBySidoName("서울특별시");

            // then
            assertThat(result).hasSize(2);
        }
    }

    private RegionCodeEntity createAndSaveRegionCode(String sidoName, String sigunguName, String lawdCd) {
        RegionCodeEntity entity = RegionCodeEntity.builder()
                .sidoName(sidoName)
                .sigunguName(sigunguName)
                .regionName(sidoName + " " + sigunguName)
                .lawdCd(lawdCd)
                .build();
        return regionCodeRepository.save(entity);
    }
}
