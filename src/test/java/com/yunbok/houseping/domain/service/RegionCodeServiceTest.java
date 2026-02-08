package com.yunbok.houseping.domain.service;

import com.yunbok.houseping.core.service.region.RegionCodeService;

import com.yunbok.houseping.entity.RegionCodeEntity;
import com.yunbok.houseping.repository.RegionCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

@DisplayName("RegionCodeService - 법정동코드 조회 서비스")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RegionCodeServiceTest {

    @Mock
    private RegionCodeRepository regionCodeRepository;

    private RegionCodeService service;

    @BeforeEach
    void setUp() {
        service = new RegionCodeService(regionCodeRepository);
    }

    @Nested
    @DisplayName("findLawdCdByAddress()")
    class FindLawdCdByAddress {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("null 또는 빈 주소는 빈 Optional을 반환한다")
        void returnsEmptyForNullOrBlankAddress(String address) {
            // when
            Optional<String> result = service.findLawdCdByAddress(address);

            // then
            assertThat(result).isEmpty();
            verify(regionCodeRepository, never()).findBySidoNameAndSigunguName(anyString(), anyString());
        }

        @Test
        @DisplayName("주소가 1개 단어면 빈 Optional을 반환한다")
        void returnsEmptyForSingleWordAddress() {
            // when
            Optional<String> result = service.findLawdCdByAddress("서울");

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("2개 단어 주소로 법정동코드를 찾는다")
        void findsLawdCdWithTwoWordAddress() {
            // given - "서울 강남구" → sido=서울, sigungu=강남구
            RegionCodeEntity entity = createEntity("서울특별시", "강남구", "11680");
            when(regionCodeRepository.findBySidoNameAndSigunguName("서울특별시", "강남구"))
                    .thenReturn(Optional.of(entity));

            // when
            Optional<String> result = service.findLawdCdByAddress("서울 강남구");

            // then
            assertThat(result).contains("11680");
        }

        @Test
        @DisplayName("3개 이상 단어 주소는 시군구를 합쳐서 조회한다")
        void findsLawdCdWithThreeWordAddress() {
            // given - "경기 수원시 장안구" → sido=경기, sigungu=수원시 장안구
            RegionCodeEntity entity = createEntity("경기도", "수원시 장안구", "41111");
            when(regionCodeRepository.findBySidoNameAndSigunguName("경기도", "수원시 장안구"))
                    .thenReturn(Optional.of(entity));

            // when
            Optional<String> result = service.findLawdCdByAddress("경기 수원시 장안구");

            // then
            assertThat(result).contains("41111");
        }

        @Test
        @DisplayName("정확한 매칭 실패 시 시군구 포함 검색으로 폴백한다")
        void fallsBackToContainingSearch() {
            // given
            RegionCodeEntity entity = createEntity("경기도", "안양시 만안구", "41171");
            when(regionCodeRepository.findBySidoNameAndSigunguName(anyString(), anyString()))
                    .thenReturn(Optional.empty());
            when(regionCodeRepository.findBySigunguNameContaining(contains("안양시")))
                    .thenReturn(List.of(entity));

            // when
            Optional<String> result = service.findLawdCdByAddress("경기 안양시");

            // then
            assertThat(result).contains("41171");
        }

        @Test
        @DisplayName("시군구 포함 검색 시 같은 시도 우선")
        void prioritizesSameSidoInContainingSearch() {
            // given
            RegionCodeEntity gyeonggi = createEntity("경기도", "안양시", "41170");
            RegionCodeEntity other = createEntity("충청남도", "안양시", "44000");
            when(regionCodeRepository.findBySidoNameAndSigunguName(anyString(), anyString()))
                    .thenReturn(Optional.empty());
            when(regionCodeRepository.findBySigunguNameContaining("안양시"))
                    .thenReturn(List.of(other, gyeonggi));

            // when
            Optional<String> result = service.findLawdCdByAddress("경기 안양시");

            // then
            assertThat(result).contains("41170");
        }

        @Test
        @DisplayName("모든 검색 실패 시 빈 Optional을 반환한다")
        void returnsEmptyWhenAllSearchesFail() {
            // given
            when(regionCodeRepository.findBySidoNameAndSigunguName(anyString(), anyString()))
                    .thenReturn(Optional.empty());
            when(regionCodeRepository.findBySigunguNameContaining(anyString()))
                    .thenReturn(List.of());

            // when
            Optional<String> result = service.findLawdCdByAddress("알수없는 지역");

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("3개 이상 단어 주소에서 2차 시도로 찾는다")
        void findsWithSecondAttemptForThreeWordAddress() {
            // given - "경기 안양시 만안구 동명" → 1차 실패, 2차("안양시 만안구")로 성공
            RegionCodeEntity entity = createEntity("경기도", "안양시 만안구", "41171");
            when(regionCodeRepository.findBySidoNameAndSigunguName(anyString(), anyString()))
                    .thenReturn(Optional.empty());
            when(regionCodeRepository.findBySigunguNameContaining("안양시 만안구"))
                    .thenReturn(List.of(entity));

            // when
            Optional<String> result = service.findLawdCdByAddress("경기 안양시 만안구 동명");

            // then
            assertThat(result).contains("41171");
        }

        @Test
        @DisplayName("3차 시도에서 시도가 다른 결과만 있으면 첫 번째를 반환한다")
        void returnsFirstMatchWhenNoSameSido() {
            // given - 3차 시도에서 시도가 다른 결과들만 있는 경우
            RegionCodeEntity other = createEntity("충청남도", "성남시", "44000");
            when(regionCodeRepository.findBySidoNameAndSigunguName(anyString(), anyString()))
                    .thenReturn(Optional.empty());
            when(regionCodeRepository.findBySigunguNameContaining("성남시"))
                    .thenReturn(List.of(other));

            // when
            Optional<String> result = service.findLawdCdByAddress("경기 성남시");

            // then
            assertThat(result).contains("44000");
        }
    }

    @Nested
    @DisplayName("시도명 정규화")
    class NormalizeSido {

        @Test
        @DisplayName("서울을 서울특별시로 정규화한다")
        void normalizesSeoul() {
            // given
            RegionCodeEntity entity = createEntity("서울특별시", "강남구", "11680");
            when(regionCodeRepository.findBySidoNameAndSigunguName("서울특별시", "강남구"))
                    .thenReturn(Optional.of(entity));

            // when
            Optional<String> result = service.findLawdCdByAddress("서울 강남구");

            // then
            assertThat(result).contains("11680");
            verify(regionCodeRepository).findBySidoNameAndSigunguName("서울특별시", "강남구");
        }

        @Test
        @DisplayName("경기를 경기도로 정규화한다")
        void normalizesGyeonggi() {
            // given
            RegionCodeEntity entity = createEntity("경기도", "성남시", "41130");
            when(regionCodeRepository.findBySidoNameAndSigunguName("경기도", "성남시"))
                    .thenReturn(Optional.of(entity));

            // when
            Optional<String> result = service.findLawdCdByAddress("경기 성남시");

            // then
            assertThat(result).contains("41130");
            verify(regionCodeRepository).findBySidoNameAndSigunguName("경기도", "성남시");
        }

        @Test
        @DisplayName("이미 정식 명칭이면 그대로 사용한다")
        void usesFullNameAsIs() {
            // given
            RegionCodeEntity entity = createEntity("서울특별시", "강남구", "11680");
            when(regionCodeRepository.findBySidoNameAndSigunguName("서울특별시", "강남구"))
                    .thenReturn(Optional.of(entity));

            // when
            Optional<String> result = service.findLawdCdByAddress("서울특별시 강남구");

            // then
            assertThat(result).contains("11680");
        }
    }

    private RegionCodeEntity createEntity(String sidoName, String sigunguName, String lawdCd) {
        return RegionCodeEntity.builder()
                .sidoName(sidoName)
                .sigunguName(sigunguName)
                .regionName(sidoName + " " + sigunguName)
                .lawdCd(lawdCd)
                .build();
    }
}
