package com.yunbok.houseping.infrastructure.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RealTransactionCacheRepository - 실거래가 캐시 Repository")
@DataJpaTest
@ActiveProfiles("test")
class RealTransactionCacheRepositoryTest {

    @Autowired
    private RealTransactionCacheRepository repository;

    @Nested
    @DisplayName("findByLawdCdAndDealYmd() - 법정동코드 + 계약월 조회")
    class FindByLawdCdAndDealYmd {

        @Test
        @DisplayName("법정동코드와 계약월로 거래 목록을 찾는다")
        void findsTransactionsByLawdCdAndDealYmd() {
            // given
            createAndSaveTransaction("11680", "202501", "아파트A", 100000L);
            createAndSaveTransaction("11680", "202501", "아파트B", 95000L);
            createAndSaveTransaction("11680", "202412", "아파트C", 90000L);

            // when
            List<RealTransactionCacheEntity> result = repository.findByLawdCdAndDealYmd("11680", "202501");

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting("aptName")
                    .containsExactlyInAnyOrder("아파트A", "아파트B");
        }
    }

    @Nested
    @DisplayName("findByLawdCdAndAptNameContaining() - 법정동코드 + 아파트명 조회")
    class FindByLawdCdAndAptNameContaining {

        @Test
        @DisplayName("법정동코드와 아파트명으로 거래를 찾는다")
        void findsTransactionsByLawdCdAndAptName() {
            // given
            createAndSaveTransaction("11680", "202501", "힐스테이트 강남", 120000L);
            createAndSaveTransaction("11680", "202501", "래미안 강남", 110000L);
            createAndSaveTransaction("11680", "202501", "자이 서초", 100000L);

            // when
            List<RealTransactionCacheEntity> result = repository
                    .findByLawdCdAndAptNameContaining("11680", "강남");

            // then
            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("existsByLawdCdAndDealYmd() - 캐시 존재 여부 확인")
    class ExistsByLawdCdAndDealYmd {

        @Test
        @DisplayName("캐시가 존재하면 true를 반환한다")
        void returnsTrueWhenCacheExists() {
            // given
            createAndSaveTransaction("11680", "202501", "아파트A", 100000L);

            // when
            boolean exists = repository.existsByLawdCdAndDealYmd("11680", "202501");

            // then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("캐시가 없으면 false를 반환한다")
        void returnsFalseWhenNoCacheExists() {
            // when
            boolean exists = repository.existsByLawdCdAndDealYmd("99999", "209912");

            // then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("RealTransactionCacheEntity - 엔티티 테스트")
    class EntityTest {

        @Test
        @DisplayName("평당 가격을 계산한다")
        void calculatesPricePerPyeong() {
            // given
            RealTransactionCacheEntity entity = RealTransactionCacheEntity.builder()
                    .lawdCd("11680")
                    .dealYmd("202501")
                    .aptName("테스트아파트")
                    .dealAmount(100000L)  // 10억
                    .excluUseAr(BigDecimal.valueOf(84.0))  // 84㎡ ≈ 25.4평
                    .build();

            // when
            Long pricePerPyeong = entity.getPricePerPyeong();

            // then
            assertThat(pricePerPyeong).isNotNull();
            assertThat(pricePerPyeong).isBetween(3900L, 4000L);  // 약 3936만원/평
        }

        @Test
        @DisplayName("전용면적이 없으면 평당가는 null을 반환한다")
        void returnsNullWhenNoArea() {
            // given
            RealTransactionCacheEntity entity = RealTransactionCacheEntity.builder()
                    .lawdCd("11680")
                    .dealYmd("202501")
                    .aptName("테스트아파트")
                    .dealAmount(100000L)
                    .excluUseAr(null)
                    .build();

            // when
            Long pricePerPyeong = entity.getPricePerPyeong();

            // then
            assertThat(pricePerPyeong).isNull();
        }
    }

    private RealTransactionCacheEntity createAndSaveTransaction(String lawdCd, String dealYmd, String aptName, Long dealAmount) {
        RealTransactionCacheEntity entity = RealTransactionCacheEntity.builder()
                .lawdCd(lawdCd)
                .dealYmd(dealYmd)
                .aptName(aptName)
                .dealAmount(dealAmount)
                .excluUseAr(BigDecimal.valueOf(84.5))
                .floor(10)
                .dealDate(LocalDate.now())
                .build();
        return repository.save(entity);
    }
}
