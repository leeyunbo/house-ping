package com.yunbok.houseping.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HouseType - 주택 유형 코드")
class HouseTypeTest {

    @Nested
    @DisplayName("getDisplayNameByCode() - 코드로 표시명 조회")
    class GetDisplayNameByCode {

        @ParameterizedTest(name = "주택구분코드 {0}의 표시명은 '{1}'이다")
        @CsvSource({
            "01, APT",
            "09, 민간사전청약",
            "10, 신혼희망타운"
        })
        @DisplayName("주택구분코드에 해당하는 표시명을 반환한다")
        void returnsCorrectDisplayName(String houseSecd, String expectedName) {
            // when
            String actualName = HouseType.getDisplayNameByCode(houseSecd);

            // then
            assertThat(actualName).isEqualTo(expectedName);
        }

        @Test
        @DisplayName("매핑되지 않은 코드는 '기타'를 반환한다")
        void returnsOtherForUnmappedCode() {
            // given
            String unknownCode = "99";

            // when
            String result = HouseType.getDisplayNameByCode(unknownCode);

            // then
            assertThat(result).isEqualTo("기타");
        }
    }

    @Nested
    @DisplayName("getCodeByDisplayName() - 표시명으로 코드 조회")
    class GetCodeByDisplayName {

        @ParameterizedTest(name = "표시명 '{0}'의 주택구분코드는 '{1}'이다")
        @CsvSource({
            "APT, 01",
            "민간사전청약, 09",
            "신혼희망타운, 10"
        })
        @DisplayName("표시명에 해당하는 주택구분코드를 반환한다")
        void returnsCorrectCode(String displayName, String expectedCode) {
            // when
            String actualCode = HouseType.getCodeByDisplayName(displayName);

            // then
            assertThat(actualCode).isEqualTo(expectedCode);
        }

        @Test
        @DisplayName("매핑되지 않은 표시명은 빈 문자열을 반환한다")
        void returnsEmptyStringForUnmappedName() {
            // given
            String unknownName = "알수없는유형";

            // when
            String result = HouseType.getCodeByDisplayName(unknownName);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Enum 필드 접근")
    class EnumFields {

        @Test
        @DisplayName("APT 유형의 코드와 표시명을 조회할 수 있다")
        void aptTypeHasCorrectValues() {
            // given
            HouseType apt = HouseType.APT;

            // then
            assertThat(apt.getHouseSecd()).isEqualTo("01");
            assertThat(apt.getDisplayName()).isEqualTo("APT");
        }

        @Test
        @DisplayName("무순위 유형은 빈 코드를 가진다")
        void remainingTypeHasEmptyCode() {
            // given
            HouseType remaining = HouseType.REMAINING;

            // then
            assertThat(remaining.getHouseSecd()).isEmpty();
            assertThat(remaining.getDisplayName()).isEqualTo("무순위");
        }
    }
}
