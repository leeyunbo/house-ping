package com.yunbok.houseping.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AreaCodeMapping - 지역 코드 매핑")
class AreaCodeMappingTest {

    @Nested
    @DisplayName("getLhAreaCodeByName() - 지역명으로 LH 코드 조회")
    class GetLhAreaCodeByName {

        @ParameterizedTest(name = "{0} 지역의 LH 코드는 {1}이다")
        @CsvSource({
            "서울, 11",
            "경기, 41",
            "인천, 28",
            "부산, 26",
            "대구, 27",
            "대전, 30",
            "광주, 29",
            "울산, 31",
            "세종, 36110",
            "강원, 42",
            "충북, 43",
            "충남, 44",
            "전북, 52",
            "전남, 46",
            "경북, 47",
            "경남, 48",
            "제주, 50"
        })
        @DisplayName("지역명에 해당하는 LH 코드를 반환한다")
        void returnsCorrectLhCode(String areaName, String expectedCode) {
            // when
            String actualCode = AreaCodeMapping.getLhAreaCodeByName(areaName);

            // then
            assertThat(actualCode).isEqualTo(expectedCode);
        }

        @Test
        @DisplayName("매핑되지 않은 지역명은 원본 값을 반환한다")
        void returnsOriginalValueForUnmappedArea() {
            // given
            String unknownArea = "알수없는지역";

            // when
            String result = AreaCodeMapping.getLhAreaCodeByName(unknownArea);

            // then
            assertThat(result).isEqualTo(unknownArea);
        }
    }

    @Nested
    @DisplayName("getAreaNameByApplyHomeCode() - 청약Home 코드로 지역명 조회")
    class GetAreaNameByApplyHomeCode {

        @ParameterizedTest(name = "청약Home 코드 {0}의 지역명은 {1}이다")
        @CsvSource({
            "100, 서울",
            "410, 경기",
            "400, 인천",
            "600, 부산",
            "700, 대구",
            "300, 대전",
            "500, 광주",
            "680, 울산",
            "338, 세종",
            "200, 강원",
            "360, 충북",
            "312, 충남",
            "560, 전북",
            "513, 전남",
            "712, 경북",
            "621, 경남",
            "690, 제주"
        })
        @DisplayName("청약Home 코드에 해당하는 지역명을 반환한다")
        void returnsCorrectAreaName(String applyHomeCode, String expectedName) {
            // when
            String actualName = AreaCodeMapping.getAreaNameByApplyHomeCode(applyHomeCode);

            // then
            assertThat(actualName).isEqualTo(expectedName);
        }

        @Test
        @DisplayName("매핑되지 않은 코드는 '알 수 없는 지역'을 반환한다")
        void returnsUnknownForUnmappedCode() {
            // given
            String unknownCode = "999";

            // when
            String result = AreaCodeMapping.getAreaNameByApplyHomeCode(unknownCode);

            // then
            assertThat(result).isEqualTo("알 수 없는 지역");
        }
    }

    @Nested
    @DisplayName("getApplyHomeCodeByName() - 지역명으로 청약Home 코드 조회")
    class GetApplyHomeCodeByName {

        @ParameterizedTest(name = "{0} 지역의 청약Home 코드는 {1}이다")
        @CsvSource({
            "서울, 100",
            "경기, 410",
            "인천, 400",
            "부산, 600"
        })
        @DisplayName("지역명에 해당하는 청약Home 코드를 반환한다")
        void returnsCorrectApplyHomeCode(String areaName, String expectedCode) {
            // when
            String actualCode = AreaCodeMapping.getApplyHomeCodeByName(areaName);

            // then
            assertThat(actualCode).isEqualTo(expectedCode);
        }

        @Test
        @DisplayName("매핑되지 않은 지역명은 원본 값을 반환한다")
        void returnsOriginalValueForUnmappedArea() {
            // given
            String unknownArea = "알수없는지역";

            // when
            String result = AreaCodeMapping.getApplyHomeCodeByName(unknownArea);

            // then
            assertThat(result).isEqualTo(unknownArea);
        }
    }

    @Nested
    @DisplayName("Enum 필드 접근")
    class EnumFields {

        @Test
        @DisplayName("서울의 모든 필드를 정상적으로 조회할 수 있다")
        void canAccessAllFieldsForSeoul() {
            // given
            AreaCodeMapping seoul = AreaCodeMapping.SEOUL;

            // then
            assertThat(seoul.getAreaName()).isEqualTo("서울");
            assertThat(seoul.getLhAreaCode()).isEqualTo("11");
            assertThat(seoul.getApplyHomeAreaCode()).isEqualTo("100");
        }
    }
}
