package com.yunbok.houseping.core.service.calendar;

import com.yunbok.houseping.entity.SubscriptionEntity;
import com.yunbok.houseping.repository.SubscriptionRepository;
import com.yunbok.houseping.support.dto.PublicCalendarEventDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("PublicCalendarService - 공개 캘린더 서비스")
@ExtendWith(MockitoExtension.class)
class PublicCalendarServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    private PublicCalendarService service;

    @BeforeEach
    void setUp() {
        service = new PublicCalendarService(subscriptionRepository);
    }

    @Nested
    @DisplayName("getCalendarEvents() - 캘린더 이벤트 조회")
    class GetCalendarEvents {

        @Test
        @DisplayName("접수 이벤트와 당첨 발표 이벤트를 모두 생성한다")
        void createsReceiptAndWinnerEvents() {
            // given
            SubscriptionEntity entity = createEntity(1L, "테스트아파트", "ApplyHome",
                    LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 5),
                    LocalDate.of(2026, 3, 10), "http://test.com");
            when(subscriptionRepository.findAll(any(com.querydsl.core.types.Predicate.class)))
                    .thenReturn(List.of(entity));

            // when
            List<PublicCalendarEventDto> events = service.getCalendarEvents(
                    LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));

            // then — 접수 + 당첨 = 2건
            assertThat(events).hasSize(2);
            assertThat(events.stream().map(e -> e.extendedProps().eventType()).toList())
                    .containsExactlyInAnyOrder("receipt", "winner");
        }

        @Test
        @DisplayName("detailUrl 기준으로 중복을 제거한다")
        void deduplicatesByDetailUrl() {
            // given
            SubscriptionEntity entity1 = createEntity(1L, "아파트A", "ApplyHome",
                    LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 5),
                    null, "http://same-url.com");
            SubscriptionEntity entity2 = createEntity(2L, "아파트A-dup", "ApplyHome",
                    LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 5),
                    null, "http://same-url.com");
            when(subscriptionRepository.findAll(any(com.querydsl.core.types.Predicate.class)))
                    .thenReturn(List.of(entity1, entity2));

            // when
            List<PublicCalendarEventDto> events = service.getCalendarEvents(
                    LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));

            // then — 중복 제거되어 1건만 (접수 이벤트)
            assertThat(events).hasSize(1);
        }

        @Test
        @DisplayName("LH 청약은 LH 색상으로 표시한다")
        void usesLhColorsForLhSubscriptions() {
            // given
            SubscriptionEntity entity = createEntity(1L, "LH아파트", "LH",
                    LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 5),
                    null, "http://lh.com");
            when(subscriptionRepository.findAll(any(com.querydsl.core.types.Predicate.class)))
                    .thenReturn(List.of(entity));

            // when
            List<PublicCalendarEventDto> events = service.getCalendarEvents(
                    LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));

            // then
            assertThat(events).hasSize(1);
            assertThat(events.get(0).color()).isEqualTo("#f97316"); // LH 접수 색상
            assertThat(events.get(0).extendedProps().hasAnalysis()).isFalse();
        }

        @Test
        @DisplayName("ApplyHome 청약은 청약 색상으로 표시하고 분석 가능하다")
        void usesApplyHomeColorsAndHasAnalysis() {
            // given
            SubscriptionEntity entity = createEntity(1L, "청약아파트", "ApplyHome",
                    LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 5),
                    null, "http://applyhome.com");
            when(subscriptionRepository.findAll(any(com.querydsl.core.types.Predicate.class)))
                    .thenReturn(List.of(entity));

            // when
            List<PublicCalendarEventDto> events = service.getCalendarEvents(
                    LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));

            // then
            assertThat(events).hasSize(1);
            assertThat(events.get(0).color()).isEqualTo("#3b82f6"); // ApplyHome 접수 색상
            assertThat(events.get(0).extendedProps().hasAnalysis()).isTrue();
        }

        @Test
        @DisplayName("만료된 접수 이벤트를 expired로 표시한다")
        void marksExpiredReceiptEvents() {
            // given
            SubscriptionEntity entity = createEntity(1L, "만료아파트", "ApplyHome",
                    LocalDate.now().minusDays(10), LocalDate.now().minusDays(5),
                    null, "http://expired.com");
            when(subscriptionRepository.findAll(any(com.querydsl.core.types.Predicate.class)))
                    .thenReturn(List.of(entity));

            // when
            List<PublicCalendarEventDto> events = service.getCalendarEvents(
                    LocalDate.now().minusDays(15), LocalDate.now().plusDays(15));

            // then
            assertThat(events).hasSize(1);
            assertThat(events.get(0).extendedProps().expired()).isTrue();
        }

        @Test
        @DisplayName("당첨 발표일만 있고 접수 시작일이 없으면 접수 이벤트를 생성하지 않는다")
        void skipsReceiptEventWhenNoStartDate() {
            // given
            SubscriptionEntity entity = SubscriptionEntity.builder()
                    .id(1L).houseName("발표전용").source("ApplyHome")
                    .winnerAnnounceDate(LocalDate.of(2026, 3, 10))
                    .detailUrl("http://test.com").build();
            when(subscriptionRepository.findAll(any(com.querydsl.core.types.Predicate.class)))
                    .thenReturn(List.of(entity));

            // when
            List<PublicCalendarEventDto> events = service.getCalendarEvents(
                    LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));

            // then — 접수 시작일이 null이므로 접수 이벤트 없음, 당첨만
            assertThat(events).hasSize(1);
            assertThat(events.get(0).extendedProps().eventType()).isEqualTo("winner");
        }
    }

    private SubscriptionEntity createEntity(Long id, String houseName, String source,
                                             LocalDate receiptStart, LocalDate receiptEnd,
                                             LocalDate winnerAnnounce, String detailUrl) {
        return SubscriptionEntity.builder()
                .id(id)
                .houseName(houseName)
                .source(source)
                .area("서울")
                .receiptStartDate(receiptStart)
                .receiptEndDate(receiptEnd)
                .winnerAnnounceDate(winnerAnnounce)
                .detailUrl(detailUrl)
                .build();
    }
}
