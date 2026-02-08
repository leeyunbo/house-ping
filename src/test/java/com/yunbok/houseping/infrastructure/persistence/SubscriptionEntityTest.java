package com.yunbok.houseping.infrastructure.persistence;

import com.yunbok.houseping.entity.SubscriptionEntity;
import com.yunbok.houseping.entity.RegionCodeEntity;
import com.yunbok.houseping.repository.RegionCodeRepository;
import com.yunbok.houseping.entity.RealTransactionCacheEntity;
import com.yunbok.houseping.repository.RealTransactionCacheRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SubscriptionEntity - 청약 정보 엔티티")
class SubscriptionEntityTest {

    @Nested
    @DisplayName("needsUpdate() - 업데이트 필요 여부 확인")
    class NeedsUpdate {

        @Test
        @DisplayName("모든 필드가 동일하면 false를 반환한다")
        void returnsFalseWhenAllFieldsAreSame() {
            // given
            SubscriptionEntity existing = createEntity("테스트 아파트",
                    LocalDate.of(2025, 1, 5),
                    LocalDate.of(2025, 1, 10),
                    LocalDate.of(2025, 1, 20),
                    "https://example.com",
                    100);

            SubscriptionEntity newEntity = createEntity("테스트 아파트",
                    LocalDate.of(2025, 1, 5),
                    LocalDate.of(2025, 1, 10),
                    LocalDate.of(2025, 1, 20),
                    "https://example.com",
                    100);

            // then
            assertThat(existing.needsUpdate(newEntity)).isFalse();
        }

        @Test
        @DisplayName("receiptStartDate가 다르면 true를 반환한다")
        void returnsTrueWhenReceiptStartDateDiffers() {
            // given
            SubscriptionEntity existing = createEntity("테스트",
                    LocalDate.of(2025, 1, 5), null, null, null, null);
            SubscriptionEntity newEntity = createEntity("테스트",
                    LocalDate.of(2025, 1, 6), null, null, null, null);

            // then
            assertThat(existing.needsUpdate(newEntity)).isTrue();
        }

        @Test
        @DisplayName("receiptEndDate가 다르면 true를 반환한다")
        void returnsTrueWhenReceiptEndDateDiffers() {
            // given
            SubscriptionEntity existing = createEntity("테스트",
                    LocalDate.of(2025, 1, 5), LocalDate.of(2025, 1, 10), null, null, null);
            SubscriptionEntity newEntity = createEntity("테스트",
                    LocalDate.of(2025, 1, 5), LocalDate.of(2025, 1, 11), null, null, null);

            // then
            assertThat(existing.needsUpdate(newEntity)).isTrue();
        }

        @Test
        @DisplayName("winnerAnnounceDate가 다르면 true를 반환한다")
        void returnsTrueWhenWinnerAnnounceDateDiffers() {
            // given
            SubscriptionEntity existing = createEntity("테스트",
                    LocalDate.of(2025, 1, 5), null, LocalDate.of(2025, 1, 20), null, null);
            SubscriptionEntity newEntity = createEntity("테스트",
                    LocalDate.of(2025, 1, 5), null, LocalDate.of(2025, 1, 21), null, null);

            // then
            assertThat(existing.needsUpdate(newEntity)).isTrue();
        }

        @Test
        @DisplayName("detailUrl이 다르면 true를 반환한다")
        void returnsTrueWhenDetailUrlDiffers() {
            // given
            SubscriptionEntity existing = createEntity("테스트",
                    LocalDate.of(2025, 1, 5), null, null, "https://old.com", null);
            SubscriptionEntity newEntity = createEntity("테스트",
                    LocalDate.of(2025, 1, 5), null, null, "https://new.com", null);

            // then
            assertThat(existing.needsUpdate(newEntity)).isTrue();
        }

        @Test
        @DisplayName("totalSupplyCount가 다르면 true를 반환한다")
        void returnsTrueWhenTotalSupplyCountDiffers() {
            // given
            SubscriptionEntity existing = createEntity("테스트",
                    LocalDate.of(2025, 1, 5), null, null, null, 100);
            SubscriptionEntity newEntity = createEntity("테스트",
                    LocalDate.of(2025, 1, 5), null, null, null, 200);

            // then
            assertThat(existing.needsUpdate(newEntity)).isTrue();
        }

        @Test
        @DisplayName("null 값끼리 비교하면 false를 반환한다")
        void returnsFalseWhenBothAreNull() {
            // given
            SubscriptionEntity existing = createEntity("테스트",
                    LocalDate.of(2025, 1, 5), null, null, null, null);
            SubscriptionEntity newEntity = createEntity("테스트",
                    LocalDate.of(2025, 1, 5), null, null, null, null);

            // then
            assertThat(existing.needsUpdate(newEntity)).isFalse();
        }

        @Test
        @DisplayName("null과 값 비교 시 true를 반환한다")
        void returnsTrueWhenOneIsNullAndOtherIsNot() {
            // given
            SubscriptionEntity existing = createEntity("테스트",
                    LocalDate.of(2025, 1, 5), null, null, null, null);
            SubscriptionEntity newEntity = createEntity("테스트",
                    LocalDate.of(2025, 1, 5), LocalDate.of(2025, 1, 10), null, null, null);

            // then
            assertThat(existing.needsUpdate(newEntity)).isTrue();
        }

        @Test
        @DisplayName("other가 null이면 false를 반환한다")
        void returnsFalseWhenOtherIsNull() {
            // given
            SubscriptionEntity existing = createEntity("테스트",
                    LocalDate.of(2025, 1, 5), null, null, null, null);

            // then
            assertThat(existing.needsUpdate(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("updateFrom() - 다른 엔티티로부터 업데이트")
    class UpdateFrom {

        @Test
        @DisplayName("다른 엔티티의 모든 필드를 복사한다")
        void copiesAllFieldsFromOther() {
            // given
            SubscriptionEntity existing = SubscriptionEntity.builder()
                    .id(1L)
                    .source("TEST")
                    .houseName("기존 아파트")
                    .houseType("APT")
                    .area("서울")
                    .receiptStartDate(LocalDate.of(2025, 1, 5))
                    .receiptEndDate(LocalDate.of(2025, 1, 10))
                    .detailUrl("https://old.com")
                    .totalSupplyCount(100)
                    .collectedAt(LocalDateTime.now())
                    .build();

            SubscriptionEntity newEntity = SubscriptionEntity.builder()
                    .houseType("신혼희망타운")
                    .area("경기")
                    .announceDate(LocalDate.of(2025, 1, 1))
                    .receiptStartDate(LocalDate.of(2025, 1, 6))
                    .receiptEndDate(LocalDate.of(2025, 1, 15))
                    .winnerAnnounceDate(LocalDate.of(2025, 1, 25))
                    .detailUrl("https://new.com")
                    .homepageUrl("https://homepage.com")
                    .contact("02-1234-5678")
                    .totalSupplyCount(200)
                    .build();

            // when
            existing.updateFrom(newEntity);

            // then
            assertThat(existing.getId()).isEqualTo(1L);  // ID는 유지
            assertThat(existing.getSource()).isEqualTo("TEST");  // source는 유지
            assertThat(existing.getHouseName()).isEqualTo("기존 아파트");  // houseName은 유지
            assertThat(existing.getHouseType()).isEqualTo("신혼희망타운");
            assertThat(existing.getArea()).isEqualTo("경기");
            assertThat(existing.getAnnounceDate()).isEqualTo(LocalDate.of(2025, 1, 1));
            assertThat(existing.getReceiptStartDate()).isEqualTo(LocalDate.of(2025, 1, 6));
            assertThat(existing.getReceiptEndDate()).isEqualTo(LocalDate.of(2025, 1, 15));
            assertThat(existing.getWinnerAnnounceDate()).isEqualTo(LocalDate.of(2025, 1, 25));
            assertThat(existing.getDetailUrl()).isEqualTo("https://new.com");
            assertThat(existing.getHomepageUrl()).isEqualTo("https://homepage.com");
            assertThat(existing.getContact()).isEqualTo("02-1234-5678");
            assertThat(existing.getTotalSupplyCount()).isEqualTo(200);
        }
    }

    @Nested
    @DisplayName("빌더를 통한 객체 생성")
    class Builder {

        @Test
        @DisplayName("필수 필드만으로 객체를 생성할 수 있다")
        void canBuildWithRequiredFieldsOnly() {
            // when
            SubscriptionEntity entity = SubscriptionEntity.builder()
                    .source("TEST")
                    .houseName("테스트 아파트")
                    .receiptStartDate(LocalDate.of(2025, 1, 5))
                    .collectedAt(LocalDateTime.now())
                    .build();

            // then
            assertThat(entity.getSource()).isEqualTo("TEST");
            assertThat(entity.getHouseName()).isEqualTo("테스트 아파트");
            assertThat(entity.getReceiptStartDate()).isEqualTo(LocalDate.of(2025, 1, 5));
        }

        @Test
        @DisplayName("모든 필드를 설정하여 객체를 생성할 수 있다")
        void canBuildWithAllFields() {
            // given
            LocalDateTime now = LocalDateTime.now();

            // when
            SubscriptionEntity entity = SubscriptionEntity.builder()
                    .id(1L)
                    .source("TEST")
                    .houseName("테스트 아파트")
                    .houseType("APT")
                    .area("서울")
                    .announceDate(LocalDate.of(2025, 1, 1))
                    .receiptStartDate(LocalDate.of(2025, 1, 5))
                    .receiptEndDate(LocalDate.of(2025, 1, 10))
                    .winnerAnnounceDate(LocalDate.of(2025, 1, 20))
                    .detailUrl("https://detail.com")
                    .homepageUrl("https://home.com")
                    .contact("02-1234-5678")
                    .totalSupplyCount(500)
                    .collectedAt(now)
                    .build();

            // then
            assertThat(entity.getId()).isEqualTo(1L);
            assertThat(entity.getSource()).isEqualTo("TEST");
            assertThat(entity.getHouseName()).isEqualTo("테스트 아파트");
            assertThat(entity.getHouseType()).isEqualTo("APT");
            assertThat(entity.getArea()).isEqualTo("서울");
            assertThat(entity.getTotalSupplyCount()).isEqualTo(500);
        }
    }

    private SubscriptionEntity createEntity(
            String houseName,
            LocalDate receiptStartDate,
            LocalDate receiptEndDate,
            LocalDate winnerAnnounceDate,
            String detailUrl,
            Integer totalSupplyCount) {
        return SubscriptionEntity.builder()
                .source("TEST")
                .houseName(houseName)
                .receiptStartDate(receiptStartDate)
                .receiptEndDate(receiptEndDate)
                .winnerAnnounceDate(winnerAnnounceDate)
                .detailUrl(detailUrl)
                .totalSupplyCount(totalSupplyCount)
                .collectedAt(LocalDateTime.now())
                .build();
    }
}
