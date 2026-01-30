package com.yunbok.houseping.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AreaNormalizer - 지역명 정규화")
class AreaNormalizerTest {

    @Nested
    @DisplayName("normalize() - 지역명을 대표 이름으로 변환")
    class Normalize {

        @Test
        @DisplayName("null은 null을 반환한다")
        void returnsNullForNull() {
            assertThat(AreaNormalizer.normalize(null)).isNull();
        }

        @ParameterizedTest
        @CsvSource({
                "서울, 서울",
                "서울특별시, 서울",
                "경기, 경기",
                "경기도, 경기",
                "인천, 인천",
                "인천광역시, 인천",
                "부산, 부산",
                "부산광역시, 부산",
                "대구, 대구",
                "대구광역시, 대구",
                "대전, 대전",
                "대전광역시, 대전",
                "광주, 광주",
                "광주광역시, 광주",
                "울산, 울산",
                "울산광역시, 울산",
                "세종, 세종",
                "세종특별자치시, 세종",
                "강원, 강원",
                "강원도, 강원",
                "강원특별자치도, 강원",
                "충북, 충북",
                "충청북도, 충북",
                "충남, 충남",
                "충청남도, 충남",
                "전북, 전북",
                "전라북도, 전북",
                "전북특별자치도, 전북",
                "전남, 전남",
                "전라남도, 전남",
                "경북, 경북",
                "경상북도, 경북",
                "경남, 경남",
                "경상남도, 경남",
                "제주, 제주",
                "제주도, 제주",
                "제주특별자치도, 제주"
        })
        @DisplayName("다양한 지역명을 대표 이름으로 변환한다")
        void normalizesVariousAreaNames(String input, String expected) {
            assertThat(AreaNormalizer.normalize(input)).isEqualTo(expected);
        }

        @Test
        @DisplayName("매핑에 없는 지역명은 원본을 반환한다")
        void returnsOriginalForUnknownArea() {
            assertThat(AreaNormalizer.normalize("미국")).isEqualTo("미국");
        }

        @Test
        @DisplayName("앞뒤 공백을 제거한다")
        void trimsWhitespace() {
            assertThat(AreaNormalizer.normalize("  서울특별시  ")).isEqualTo("서울");
        }
    }

    @Nested
    @DisplayName("expand() - 대표 이름의 모든 별칭 반환")
    class Expand {

        @Test
        @DisplayName("null은 빈 리스트를 반환한다")
        void returnsEmptyListForNull() {
            assertThat(AreaNormalizer.expand(null)).isEmpty();
        }

        @Test
        @DisplayName("서울의 별칭을 모두 반환한다")
        void expandsSeoul() {
            List<String> result = AreaNormalizer.expand("서울");
            assertThat(result).containsExactly("서울", "서울특별시");
        }

        @Test
        @DisplayName("서울특별시도 서울의 별칭을 반환한다")
        void expandsSeoulFullName() {
            List<String> result = AreaNormalizer.expand("서울특별시");
            assertThat(result).containsExactly("서울", "서울특별시");
        }

        @Test
        @DisplayName("강원의 별칭을 모두 반환한다")
        void expandsGangwon() {
            List<String> result = AreaNormalizer.expand("강원");
            assertThat(result).containsExactly("강원", "강원도", "강원특별자치도");
        }

        @Test
        @DisplayName("제주의 별칭을 모두 반환한다")
        void expandsJeju() {
            List<String> result = AreaNormalizer.expand("제주");
            assertThat(result).containsExactly("제주", "제주도", "제주특별자치도");
        }

        @Test
        @DisplayName("전북의 별칭을 모두 반환한다")
        void expandsJeonbuk() {
            List<String> result = AreaNormalizer.expand("전북");
            assertThat(result).containsExactly("전북", "전라북도", "전북특별자치도");
        }

        @Test
        @DisplayName("매핑에 없는 지역명은 해당 이름만 포함된 리스트를 반환한다")
        void returnsListWithOriginalForUnknownArea() {
            List<String> result = AreaNormalizer.expand("미국");
            assertThat(result).containsExactly("미국");
        }
    }
}
