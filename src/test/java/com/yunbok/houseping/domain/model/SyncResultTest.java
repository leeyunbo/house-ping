package com.yunbok.houseping.domain.model;

import com.yunbok.houseping.support.dto.SyncResult;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SyncResult - 동기화 결과 Value Object")
class SyncResultTest {

    @Nested
    @DisplayName("empty() - 빈 결과 생성")
    class Empty {

        @Test
        @DisplayName("모든 카운트가 0인 결과를 반환한다")
        void returnsZeroCountsForAllFields() {
            // when
            SyncResult result = SyncResult.empty();

            // then
            assertThat(result.inserted()).isZero();
            assertThat(result.updated()).isZero();
            assertThat(result.skipped()).isZero();
        }
    }

    @Nested
    @DisplayName("total() - 전체 처리 건수 계산")
    class Total {

        @Test
        @DisplayName("inserted + updated + skipped 합계를 반환한다")
        void returnsSumOfAllCounts() {
            // given
            SyncResult result = new SyncResult(10, 5, 3);

            // when
            int total = result.total();

            // then
            assertThat(total).isEqualTo(18);
        }

        @Test
        @DisplayName("빈 결과의 합계는 0이다")
        void emptyResultReturnsZero() {
            // given
            SyncResult result = SyncResult.empty();

            // when
            int total = result.total();

            // then
            assertThat(total).isZero();
        }
    }

    @Nested
    @DisplayName("hasChanges() - 변경사항 여부 확인")
    class HasChanges {

        @Test
        @DisplayName("inserted가 0보다 크면 true를 반환한다")
        void returnsTrueWhenInsertedIsPositive() {
            // given
            SyncResult result = new SyncResult(1, 0, 0);

            // then
            assertThat(result.hasChanges()).isTrue();
        }

        @Test
        @DisplayName("updated가 0보다 크면 true를 반환한다")
        void returnsTrueWhenUpdatedIsPositive() {
            // given
            SyncResult result = new SyncResult(0, 1, 0);

            // then
            assertThat(result.hasChanges()).isTrue();
        }

        @Test
        @DisplayName("skipped만 있고 inserted와 updated가 0이면 false를 반환한다")
        void returnsFalseWhenOnlySkipped() {
            // given
            SyncResult result = new SyncResult(0, 0, 10);

            // then
            assertThat(result.hasChanges()).isFalse();
        }

        @Test
        @DisplayName("빈 결과는 false를 반환한다")
        void emptyResultReturnsFalse() {
            // given
            SyncResult result = SyncResult.empty();

            // then
            assertThat(result.hasChanges()).isFalse();
        }
    }

    @Nested
    @DisplayName("merge() - 두 결과 병합")
    class Merge {

        @Test
        @DisplayName("두 결과의 각 카운트를 합산한다")
        void sumsBothResultsCounts() {
            // given
            SyncResult first = new SyncResult(10, 5, 3);
            SyncResult second = new SyncResult(7, 2, 8);

            // when
            SyncResult merged = first.merge(second);

            // then
            assertThat(merged.inserted()).isEqualTo(17);
            assertThat(merged.updated()).isEqualTo(7);
            assertThat(merged.skipped()).isEqualTo(11);
        }

        @Test
        @DisplayName("빈 결과와 병합하면 원본 값이 유지된다")
        void mergeWithEmptyKeepsOriginalValues() {
            // given
            SyncResult original = new SyncResult(10, 5, 3);
            SyncResult empty = SyncResult.empty();

            // when
            SyncResult merged = original.merge(empty);

            // then
            assertThat(merged.inserted()).isEqualTo(10);
            assertThat(merged.updated()).isEqualTo(5);
            assertThat(merged.skipped()).isEqualTo(3);
        }

        @Test
        @DisplayName("병합은 새로운 인스턴스를 반환한다 (불변성 보장)")
        void mergeReturnsNewInstance() {
            // given
            SyncResult first = new SyncResult(10, 5, 3);
            SyncResult second = new SyncResult(7, 2, 8);

            // when
            SyncResult merged = first.merge(second);

            // then
            assertThat(merged).isNotSameAs(first);
            assertThat(merged).isNotSameAs(second);
        }
    }
}
