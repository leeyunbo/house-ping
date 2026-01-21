package com.yunbok.houseping.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LhApiTypeCode - LH API 공고유형 코드")
class LhApiTypeCodeTest {

    @Nested
    @DisplayName("Enum 값 정의")
    class EnumValues {

        @ParameterizedTest(name = "{0}의 공고유형코드는 {1}, 표시명은 '{2}'이다")
        @MethodSource("lhApiTypeCodeProvider")
        @DisplayName("각 유형의 코드와 표시명이 올바르게 정의되어 있다")
        void hasCorrectCodeAndDisplayName(LhApiTypeCode type, String expectedCode, String expectedName) {
            // then
            assertThat(type.getTypeCode()).isEqualTo(expectedCode);
            assertThat(type.getDisplayName()).isEqualTo(expectedName);
        }

        static Stream<Arguments> lhApiTypeCodeProvider() {
            return Stream.of(
                Arguments.of(LhApiTypeCode.SALE_APT, "05", "LH 분양주택"),
                Arguments.of(LhApiTypeCode.NEWLYWED_APT, "39", "LH 신혼희망타운"),
                Arguments.of(LhApiTypeCode.RENTAL_APT, "06", "LH 임대주택")
            );
        }
    }

    @Nested
    @DisplayName("개별 유형 검증")
    class IndividualTypes {

        @Test
        @DisplayName("분양주택(SALE_APT)은 코드 '05'를 가진다")
        void saleAptHasCode05() {
            // given
            LhApiTypeCode saleApt = LhApiTypeCode.SALE_APT;

            // then
            assertThat(saleApt.getTypeCode()).isEqualTo("05");
            assertThat(saleApt.getDisplayName()).isEqualTo("LH 분양주택");
        }

        @Test
        @DisplayName("신혼희망타운(NEWLYWED_APT)은 코드 '39'를 가진다")
        void newlywedAptHasCode39() {
            // given
            LhApiTypeCode newlywedApt = LhApiTypeCode.NEWLYWED_APT;

            // then
            assertThat(newlywedApt.getTypeCode()).isEqualTo("39");
            assertThat(newlywedApt.getDisplayName()).isEqualTo("LH 신혼희망타운");
        }

        @Test
        @DisplayName("임대주택(RENTAL_APT)은 코드 '06'를 가진다")
        void rentalAptHasCode06() {
            // given
            LhApiTypeCode rentalApt = LhApiTypeCode.RENTAL_APT;

            // then
            assertThat(rentalApt.getTypeCode()).isEqualTo("06");
            assertThat(rentalApt.getDisplayName()).isEqualTo("LH 임대주택");
        }
    }

    @Test
    @DisplayName("총 3개의 공고유형이 정의되어 있다")
    void hasThreeTypes() {
        assertThat(LhApiTypeCode.values()).hasSize(3);
    }
}
