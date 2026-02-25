package com.yunbok.houseping.service.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AdminSubscriptionSearchCriteria - 관리자 검색 조건")
class AdminSubscriptionSearchCriteriaTest {

    @Nested
    @DisplayName("페이지 번호 정규화")
    class PageNormalization {

        @Test
        @DisplayName("음수 페이지 번호는 0으로 정규화된다")
        void normalizesNegativePageToZero() {
            // when
            AdminSubscriptionSearchCriteria criteria = new AdminSubscriptionSearchCriteria(
                    null, null, null, null, null, null,-5, 20
            );

            // then
            assertThat(criteria.page()).isZero();
        }

        @Test
        @DisplayName("양수 페이지 번호는 그대로 유지된다")
        void keepsPositivePageNumber() {
            // when
            AdminSubscriptionSearchCriteria criteria = new AdminSubscriptionSearchCriteria(
                    null, null, null, null, null, null,3, 20
            );

            // then
            assertThat(criteria.page()).isEqualTo(3);
        }

        @Test
        @DisplayName("0 페이지 번호는 그대로 유지된다")
        void keepsZeroPageNumber() {
            // when
            AdminSubscriptionSearchCriteria criteria = new AdminSubscriptionSearchCriteria(
                    null, null, null, null, null, null,0, 20
            );

            // then
            assertThat(criteria.page()).isZero();
        }
    }

    @Nested
    @DisplayName("페이지 사이즈 정규화")
    class SizeNormalization {

        @Test
        @DisplayName("0 이하 사이즈는 기본값 20으로 설정된다")
        void normalizesZeroOrNegativeSizeToDefault() {
            // when
            AdminSubscriptionSearchCriteria criteriaZero = new AdminSubscriptionSearchCriteria(
                    null, null, null, null, null, null,0, 0
            );
            AdminSubscriptionSearchCriteria criteriaNegative = new AdminSubscriptionSearchCriteria(
                    null, null, null, null, null, null,0, -10
            );

            // then
            assertThat(criteriaZero.size()).isEqualTo(20);
            assertThat(criteriaNegative.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("100 초과 사이즈는 100으로 제한된다")
        void limitsExcessiveSizeToMax() {
            // when
            AdminSubscriptionSearchCriteria criteria = new AdminSubscriptionSearchCriteria(
                    null, null, null, null, null, null,0, 500
            );

            // then
            assertThat(criteria.size()).isEqualTo(100);
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 20, 50, 100})
        @DisplayName("1~100 사이의 사이즈는 그대로 유지된다")
        void keepsValidSize(int size) {
            // when
            AdminSubscriptionSearchCriteria criteria = new AdminSubscriptionSearchCriteria(
                    null, null, null, null, null, null,0, size
            );

            // then
            assertThat(criteria.size()).isEqualTo(size);
        }
    }

    @Nested
    @DisplayName("검색 조건 필드")
    class SearchFields {

        @Test
        @DisplayName("모든 검색 조건이 정상적으로 설정된다")
        void setsAllSearchFields() {
            // given
            String keyword = "힐스테이트";
            String area = "서울";
            String source = "ApplyHome";
            LocalDate startDate = LocalDate.of(2025, 1, 1);
            LocalDate endDate = LocalDate.of(2025, 12, 31);

            // when
            AdminSubscriptionSearchCriteria criteria = new AdminSubscriptionSearchCriteria(
                    keyword, area, null, source, startDate, endDate, 0, 20
            );

            // then
            assertThat(criteria.keyword()).isEqualTo(keyword);
            assertThat(criteria.area()).isEqualTo(area);
            assertThat(criteria.source()).isEqualTo(source);
            assertThat(criteria.startDate()).isEqualTo(startDate);
            assertThat(criteria.endDate()).isEqualTo(endDate);
        }

        @Test
        @DisplayName("null 값들이 허용된다")
        void allowsNullValues() {
            // when
            AdminSubscriptionSearchCriteria criteria = new AdminSubscriptionSearchCriteria(
                    null, null, null, null, null, null,0, 20
            );

            // then
            assertThat(criteria.keyword()).isNull();
            assertThat(criteria.area()).isNull();
            assertThat(criteria.source()).isNull();
            assertThat(criteria.startDate()).isNull();
            assertThat(criteria.endDate()).isNull();
        }
    }
}
