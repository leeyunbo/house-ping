package com.yunbok.houseping.core.service.region;

import com.yunbok.houseping.entity.RegionCodeEntity;
import com.yunbok.houseping.repository.RegionCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("RegionCodeService - 법정동코드 조회 서비스")
@ExtendWith(MockitoExtension.class)
class RegionCodeServiceTest {

    @Mock
    private RegionCodeRepository regionCodeRepository;

    private RegionCodeService service;

    @BeforeEach
    void setUp() {
        service = new RegionCodeService(regionCodeRepository);
    }

    @Nested
    @DisplayName("findLawdCdByAddress() - 주소에서 법정동코드 추출")
    class FindLawdCdByAddress {

        @Test
        @DisplayName("시도+시군구로 정확히 매칭한다")
        void matchesExactSidoAndSigungu() {
            // given
            RegionCodeEntity entity = createRegion("경기도", "안양시 만안구", "41171");
            when(regionCodeRepository.findBySidoNameAndSigunguName("경기도", "안양시 만안구"))
                    .thenReturn(Optional.of(entity));

            // when
            Optional<String> result = service.findLawdCdByAddress("경기 안양시 만안구 안양2동 841-5번지");

            // then
            assertThat(result).isPresent().contains("41171");
        }

        @Test
        @DisplayName("약칭 시도명을 정규화하여 조회한다 (서울 → 서울특별시)")
        void normalizesSidoName() {
            // given
            RegionCodeEntity entity = createRegion("서울특별시", "강남구", "11680");
            when(regionCodeRepository.findBySidoNameAndSigunguName("서울특별시", "강남구 역삼동"))
                    .thenReturn(Optional.empty());
            when(regionCodeRepository.findBySigunguNameContaining("강남구 역삼동"))
                    .thenReturn(List.of());
            when(regionCodeRepository.findBySigunguNameContaining("강남구"))
                    .thenReturn(List.of(entity));

            // when
            Optional<String> result = service.findLawdCdByAddress("서울 강남구 역삼동 123");

            // then
            assertThat(result).isPresent().contains("11680");
        }

        @Test
        @DisplayName("이미 정규화된 시도명도 정상 처리한다")
        void handlesFullSidoName() {
            // given
            RegionCodeEntity entity = createRegion("서울특별시", "서초구", "11650");
            when(regionCodeRepository.findBySidoNameAndSigunguName("서울특별시", "서초구 반포동"))
                    .thenReturn(Optional.empty());
            when(regionCodeRepository.findBySigunguNameContaining("서초구 반포동"))
                    .thenReturn(List.of());
            when(regionCodeRepository.findBySigunguNameContaining("서초구"))
                    .thenReturn(List.of(entity));

            // when
            Optional<String> result = service.findLawdCdByAddress("서울특별시 서초구 반포동 100");

            // then
            assertThat(result).isPresent().contains("11650");
        }

        @Test
        @DisplayName("2차 시도: 시군구 부분 매칭으로 찾는다")
        void fallsBackToSigunguContaining() {
            // given
            RegionCodeEntity entity = createRegion("경기도", "수원시 장안구", "41111");
            when(regionCodeRepository.findBySidoNameAndSigunguName("경기도", "수원시 장안구"))
                    .thenReturn(Optional.empty());
            when(regionCodeRepository.findBySigunguNameContaining("수원시 장안구"))
                    .thenReturn(List.of(entity));

            // when
            Optional<String> result = service.findLawdCdByAddress("경기 수원시 장안구 정자동");

            // then
            assertThat(result).isPresent().contains("41111");
        }

        @Test
        @DisplayName("3차 시도: 시군구 첫 부분으로 찾되 같은 시도 우선")
        void fallsBackToFirstPartWithSidoPriority() {
            // given
            RegionCodeEntity gyeonggi = createRegion("경기도", "안양시 만안구", "41171");
            RegionCodeEntity other = createRegion("충청남도", "안양면", "33333");
            when(regionCodeRepository.findBySidoNameAndSigunguName("경기도", "안양시 만안구"))
                    .thenReturn(Optional.empty());
            when(regionCodeRepository.findBySigunguNameContaining("안양시 만안구"))
                    .thenReturn(List.of());
            when(regionCodeRepository.findBySigunguNameContaining("안양시"))
                    .thenReturn(List.of(other, gyeonggi));

            // when
            Optional<String> result = service.findLawdCdByAddress("경기 안양시 만안구 안양동");

            // then
            assertThat(result).isPresent().contains("41171");
        }

        @Test
        @DisplayName("모든 시도에서 못 찾으면 empty 반환")
        void returnsEmptyWhenNotFound() {
            // given
            when(regionCodeRepository.findBySidoNameAndSigunguName(anyString(), anyString()))
                    .thenReturn(Optional.empty());
            when(regionCodeRepository.findBySigunguNameContaining(anyString()))
                    .thenReturn(List.of());

            // when
            Optional<String> result = service.findLawdCdByAddress("존재하지않는 지역 주소");

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("null 주소이면 empty 반환")
        void returnsEmptyForNull() {
            Optional<String> result = service.findLawdCdByAddress(null);

            assertThat(result).isEmpty();
            verifyNoInteractions(regionCodeRepository);
        }

        @Test
        @DisplayName("빈 문자열 주소이면 empty 반환")
        void returnsEmptyForBlank() {
            Optional<String> result = service.findLawdCdByAddress("   ");

            assertThat(result).isEmpty();
            verifyNoInteractions(regionCodeRepository);
        }

        @Test
        @DisplayName("단어 하나뿐인 주소이면 empty 반환")
        void returnsEmptyForSingleWord() {
            Optional<String> result = service.findLawdCdByAddress("서울");

            assertThat(result).isEmpty();
            verifyNoInteractions(regionCodeRepository);
        }

        @Test
        @DisplayName("두 단어 주소는 시도+시군구로 조회한다")
        void handlesTwoWordAddress() {
            // given
            RegionCodeEntity entity = createRegion("서울특별시", "강남구", "11680");
            when(regionCodeRepository.findBySidoNameAndSigunguName("서울특별시", "강남구"))
                    .thenReturn(Optional.of(entity));

            // when
            Optional<String> result = service.findLawdCdByAddress("서울 강남구");

            // then
            assertThat(result).isPresent().contains("11680");
        }
    }

    private RegionCodeEntity createRegion(String sidoName, String sigunguName, String lawdCd) {
        return RegionCodeEntity.builder()
                .sidoName(sidoName)
                .sigunguName(sigunguName)
                .lawdCd(lawdCd)
                .build();
    }
}
