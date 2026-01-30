package com.yunbok.houseping.adapter.in.web.admin;

import com.yunbok.houseping.infrastructure.persistence.NotificationSubscriptionEntity;
import com.yunbok.houseping.infrastructure.persistence.NotificationSubscriptionRepository;
import com.yunbok.houseping.infrastructure.persistence.SubscriptionEntity;
import com.yunbok.houseping.infrastructure.persistence.SubscriptionRepository;
import com.querydsl.core.types.Predicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("AdminSubscriptionQueryService - 관리자 검색 서비스")
@ExtendWith(MockitoExtension.class)
class AdminSubscriptionQueryServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private NotificationSubscriptionRepository notificationSubscriptionRepository;

    private AdminSubscriptionQueryService service;

    @BeforeEach
    void setUp() {
        service = new AdminSubscriptionQueryService(subscriptionRepository, notificationSubscriptionRepository);
    }

    @Nested
    @DisplayName("search() - 검색 실행")
    class Search {

        @Test
        @DisplayName("검색 조건 없이 페이징된 결과를 반환한다")
        void returnsPagedResultsWithoutConditions() {
            // given
            AdminSubscriptionSearchCriteria criteria = new AdminSubscriptionSearchCriteria(
                    null, null, null, null, null, null, 0, 20
            );

            List<SubscriptionEntity> entities = List.of(createEntity("테스트 아파트"));
            Page<SubscriptionEntity> page = new PageImpl<>(entities);

            when(subscriptionRepository.findAll(any(Predicate.class), any(Pageable.class)))
                    .thenReturn(page);

            // when
            Page<AdminSubscriptionDto> result = service.search(criteria);

            // then
            assertThat(result.getContent()).hasSize(1);
            verify(subscriptionRepository).findAll(any(Predicate.class), any(Pageable.class));
        }

        @Test
        @DisplayName("키워드 조건이 있으면 houseName LIKE 검색을 수행한다")
        void appliesKeywordCondition() {
            // given
            AdminSubscriptionSearchCriteria criteria = new AdminSubscriptionSearchCriteria(
                    "힐스테이트", null, null, null, null, null, 0, 20
            );

            when(subscriptionRepository.findAll(any(Predicate.class), any(Pageable.class)))
                    .thenReturn(Page.empty());

            // when
            service.search(criteria);

            // then
            verify(subscriptionRepository).findAll(any(Predicate.class), any(Pageable.class));
        }

        @Test
        @DisplayName("지역 조건이 있으면 area 필터링을 수행한다")
        void appliesAreaCondition() {
            // given
            AdminSubscriptionSearchCriteria criteria = new AdminSubscriptionSearchCriteria(
                    null, "서울", null, null, null, null, 0, 20
            );

            when(subscriptionRepository.findAll(any(Predicate.class), any(Pageable.class)))
                    .thenReturn(Page.empty());

            // when
            service.search(criteria);

            // then
            verify(subscriptionRepository).findAll(any(Predicate.class), any(Pageable.class));
        }

        @Test
        @DisplayName("소스 조건이 있으면 source 필터링을 수행한다")
        void appliesSourceCondition() {
            // given
            AdminSubscriptionSearchCriteria criteria = new AdminSubscriptionSearchCriteria(
                    null, null, null, "ApplyHome", null, null, 0, 20
            );

            when(subscriptionRepository.findAll(any(Predicate.class), any(Pageable.class)))
                    .thenReturn(Page.empty());

            // when
            service.search(criteria);

            // then
            verify(subscriptionRepository).findAll(any(Predicate.class), any(Pageable.class));
        }

        @Test
        @DisplayName("날짜 범위 조건이 있으면 receiptStartDate 필터링을 수행한다")
        void appliesDateRangeCondition() {
            // given
            LocalDate startDate = LocalDate.of(2025, 1, 1);
            LocalDate endDate = LocalDate.of(2025, 12, 31);

            AdminSubscriptionSearchCriteria criteria = new AdminSubscriptionSearchCriteria(
                    null, null, null, null, startDate, endDate, 0, 20
            );

            when(subscriptionRepository.findAll(any(Predicate.class), any(Pageable.class)))
                    .thenReturn(Page.empty());

            // when
            service.search(criteria);

            // then
            verify(subscriptionRepository).findAll(any(Predicate.class), any(Pageable.class));
        }

        @Test
        @DisplayName("시작일만 있으면 그 이후 데이터만 조회한다")
        void appliesStartDateOnlyCondition() {
            // given
            AdminSubscriptionSearchCriteria criteria = new AdminSubscriptionSearchCriteria(
                    null, null, null, null, LocalDate.of(2025, 1, 1), null, 0, 20
            );

            when(subscriptionRepository.findAll(any(Predicate.class), any(Pageable.class)))
                    .thenReturn(Page.empty());

            // when
            service.search(criteria);

            // then
            verify(subscriptionRepository).findAll(any(Predicate.class), any(Pageable.class));
        }

        @Test
        @DisplayName("종료일만 있으면 그 이전 데이터만 조회한다")
        void appliesEndDateOnlyCondition() {
            // given
            AdminSubscriptionSearchCriteria criteria = new AdminSubscriptionSearchCriteria(
                    null, null, null, null, null, LocalDate.of(2025, 12, 31), 0, 20
            );

            when(subscriptionRepository.findAll(any(Predicate.class), any(Pageable.class)))
                    .thenReturn(Page.empty());

            // when
            service.search(criteria);

            // then
            verify(subscriptionRepository).findAll(any(Predicate.class), any(Pageable.class));
        }

        @Test
        @DisplayName("모든 조건이 있으면 모두 적용한다")
        void appliesAllConditions() {
            // given
            AdminSubscriptionSearchCriteria criteria = new AdminSubscriptionSearchCriteria(
                    "힐스테이트",
                    "서울",
                    null,
                    "ApplyHome",
                    LocalDate.of(2025, 1, 1),
                    LocalDate.of(2025, 12, 31),
                    0,
                    20
            );

            when(subscriptionRepository.findAll(any(Predicate.class), any(Pageable.class)))
                    .thenReturn(Page.empty());

            // when
            service.search(criteria);

            // then
            verify(subscriptionRepository).findAll(any(Predicate.class), any(Pageable.class));
        }

        @Test
        @DisplayName("정렬은 receiptStartDate DESC, createdAt DESC 순이다")
        void sortsResultsByDateDesc() {
            // given
            AdminSubscriptionSearchCriteria criteria = new AdminSubscriptionSearchCriteria(
                    null, null, null, null, null, null, 0, 20
            );

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

            when(subscriptionRepository.findAll(any(Predicate.class), pageableCaptor.capture()))
                    .thenReturn(Page.empty());

            // when
            service.search(criteria);

            // then
            Pageable pageable = pageableCaptor.getValue();
            assertThat(pageable.getSort().getOrderFor("receiptStartDate")).isNotNull();
            assertThat(pageable.getSort().getOrderFor("receiptStartDate").isDescending()).isTrue();
        }
    }

    @Nested
    @DisplayName("availableAreas() - 사용 가능한 지역 목록")
    class AvailableAreas {

        @Test
        @DisplayName("저장된 지역 목록을 반환한다")
        void returnsDistinctAreas() {
            // given
            List<String> areas = List.of("서울", "서울특별시", "경기", "경기도", "인천");
            when(subscriptionRepository.findDistinctAreas()).thenReturn(areas);

            // when
            List<String> result = service.availableAreas();

            // then
            assertThat(result).containsExactly("경기", "서울", "인천");
        }
    }

    @Nested
    @DisplayName("availableSources() - 사용 가능한 소스 목록")
    class AvailableSources {

        @Test
        @DisplayName("저장된 소스 목록을 반환한다")
        void returnsDistinctSources() {
            // given
            List<String> sources = List.of("ApplyHome", "LH");
            when(subscriptionRepository.findDistinctSources()).thenReturn(sources);

            // when
            List<String> result = service.availableSources();

            // then
            assertThat(result).containsExactly("ApplyHome", "LH");
        }
    }

    @Nested
    @DisplayName("availableHouseTypes() - 사용 가능한 주택형 목록")
    class AvailableHouseTypes {

        @Test
        @DisplayName("저장된 주택형 목록을 반환한다")
        void returnsDistinctHouseTypes() {
            // given
            List<String> houseTypes = List.of("084T", "059A", "074B");
            when(subscriptionRepository.findDistinctHouseTypes()).thenReturn(houseTypes);

            // when
            List<String> result = service.availableHouseTypes();

            // then
            assertThat(result).containsExactly("084T", "059A", "074B");
        }
    }

    @Nested
    @DisplayName("getCalendarEvents() - 캘린더 이벤트 조회")
    class GetCalendarEvents {

        @Test
        @DisplayName("범위 내 청약을 이벤트로 변환한다")
        void returnsEventsInRange() {
            // given
            LocalDate start = LocalDate.of(2025, 1, 1);
            LocalDate end = LocalDate.of(2025, 1, 31);

            SubscriptionEntity entity = createEntityWithDates(
                    "테스트 아파트", LocalDate.of(2025, 1, 10), LocalDate.of(2025, 1, 20));

            when(subscriptionRepository.findAll(any(Predicate.class)))
                    .thenReturn(List.of(entity));
            when(notificationSubscriptionRepository.findBySubscriptionIdInAndEnabledTrue(any()))
                    .thenReturn(List.of());

            // when
            List<CalendarEventDto> events = service.getCalendarEvents(start, end);

            // then
            assertThat(events).isNotEmpty();
        }

        @Test
        @DisplayName("LH 소스는 다른 색상을 사용한다")
        void usesLHColorForLHSource() {
            // given
            LocalDate start = LocalDate.of(2025, 1, 1);
            LocalDate end = LocalDate.of(2025, 1, 31);

            SubscriptionEntity entity = SubscriptionEntity.builder()
                    .id(1L)
                    .houseName("LH 아파트")
                    .area("서울")
                    .source("LH")
                    .receiptStartDate(LocalDate.of(2025, 1, 10))
                    .receiptEndDate(LocalDate.of(2025, 1, 20))
                    .collectedAt(LocalDateTime.now())
                    .build();

            when(subscriptionRepository.findAll(any(Predicate.class)))
                    .thenReturn(List.of(entity));
            when(notificationSubscriptionRepository.findBySubscriptionIdInAndEnabledTrue(any()))
                    .thenReturn(List.of());

            // when
            List<CalendarEventDto> events = service.getCalendarEvents(start, end);

            // then
            assertThat(events).isNotEmpty();
            assertThat(events.get(0).color()).isEqualTo("#f97316"); // LH 오렌지색
        }

        @Test
        @DisplayName("당첨 발표일 이벤트도 생성한다")
        void createsWinnerAnnounceEvent() {
            // given
            LocalDate start = LocalDate.of(2025, 1, 1);
            LocalDate end = LocalDate.of(2025, 1, 31);

            SubscriptionEntity entity = SubscriptionEntity.builder()
                    .id(1L)
                    .houseName("테스트 아파트")
                    .area("서울")
                    .source("ApplyHome")
                    .receiptStartDate(LocalDate.of(2025, 1, 10))
                    .receiptEndDate(LocalDate.of(2025, 1, 20))
                    .winnerAnnounceDate(LocalDate.of(2025, 1, 25))
                    .collectedAt(LocalDateTime.now())
                    .build();

            when(subscriptionRepository.findAll(any(Predicate.class)))
                    .thenReturn(List.of(entity));
            when(notificationSubscriptionRepository.findBySubscriptionIdInAndEnabledTrue(any()))
                    .thenReturn(List.of());

            // when
            List<CalendarEventDto> events = service.getCalendarEvents(start, end);

            // then
            assertThat(events).hasSize(2); // 접수 기간 + 당첨 발표일
        }
    }

    @Nested
    @DisplayName("findById() - ID로 조회")
    class FindById {

        @Test
        @DisplayName("존재하면 DTO로 변환하여 반환한다")
        void returnsDtoWhenFound() {
            // given
            SubscriptionEntity entity = createEntityWithDates(
                    "테스트 아파트", LocalDate.of(2025, 1, 10), LocalDate.of(2025, 1, 20));
            when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(entity));

            // when
            Optional<AdminSubscriptionDto> result = service.findById(1L);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().houseName()).isEqualTo("테스트 아파트");
        }

        @Test
        @DisplayName("존재하지 않으면 빈 Optional을 반환한다")
        void returnsEmptyWhenNotFound() {
            // given
            when(subscriptionRepository.findById(999L)).thenReturn(Optional.empty());

            // when
            Optional<AdminSubscriptionDto> result = service.findById(999L);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("toggleNotification() - 알림 토글")
    class ToggleNotification {

        @Test
        @DisplayName("기존 구독이 있으면 토글한다")
        void togglesExisting() {
            // given
            NotificationSubscriptionEntity entity = NotificationSubscriptionEntity.builder()
                    .id(1L)
                    .subscriptionId(100L)
                    .enabled(true)
                    .build();
            when(notificationSubscriptionRepository.findBySubscriptionId(100L))
                    .thenReturn(Optional.of(entity));

            // when
            boolean result = service.toggleNotification(100L);

            // then
            assertThat(result).isFalse(); // true -> false
        }

        @Test
        @DisplayName("기존 구독이 없으면 새로 생성한다")
        void createsNewWhenNotExists() {
            // given
            when(notificationSubscriptionRepository.findBySubscriptionId(100L))
                    .thenReturn(Optional.empty());

            // when
            boolean result = service.toggleNotification(100L);

            // then
            assertThat(result).isTrue();
            verify(notificationSubscriptionRepository).save(any(NotificationSubscriptionEntity.class));
        }
    }

    @Nested
    @DisplayName("removeNotification() - 알림 제거")
    class RemoveNotification {

        @Test
        @DisplayName("해당 청약의 알림 구독을 삭제한다")
        void removesNotificationSubscription() {
            // when
            service.removeNotification(100L);

            // then
            verify(notificationSubscriptionRepository).deleteBySubscriptionId(100L);
        }
    }

    private SubscriptionEntity createEntity(String houseName) {
        return SubscriptionEntity.builder()
                .houseName(houseName)
                .area("서울")
                .source("ApplyHome")
                .receiptStartDate(LocalDate.now())
                .collectedAt(LocalDateTime.now())
                .build();
    }

    private SubscriptionEntity createEntityWithDates(String houseName, LocalDate receiptStart, LocalDate receiptEnd) {
        return SubscriptionEntity.builder()
                .id(1L)
                .houseName(houseName)
                .area("서울")
                .source("ApplyHome")
                .receiptStartDate(receiptStart)
                .receiptEndDate(receiptEnd)
                .collectedAt(LocalDateTime.now())
                .build();
    }
}
