package com.yunbok.houseping.adapter.in.web.admin;

import com.yunbok.houseping.infrastructure.persistence.SubscriptionEntity;
import com.yunbok.houseping.infrastructure.persistence.SubscriptionRepository;
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
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("AdminSubscriptionQueryService - 관리자 검색 서비스")
@ExtendWith(MockitoExtension.class)
class AdminSubscriptionQueryServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    private AdminSubscriptionQueryService service;

    @BeforeEach
    void setUp() {
        service = new AdminSubscriptionQueryService(subscriptionRepository);
    }

    @Nested
    @DisplayName("search() - 검색 실행")
    class Search {

        @Test
        @DisplayName("검색 조건 없이 페이징된 결과를 반환한다")
        void returnsPagedResultsWithoutConditions() {
            // given
            AdminSubscriptionSearchCriteria criteria = new AdminSubscriptionSearchCriteria(
                    null, null, null, null, null, 0, 20
            );

            List<SubscriptionEntity> entities = List.of(createEntity("테스트 아파트"));
            Page<SubscriptionEntity> page = new PageImpl<>(entities);

            when(subscriptionRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(page);

            // when
            Page<AdminSubscriptionDto> result = service.search(criteria);

            // then
            assertThat(result.getContent()).hasSize(1);
            verify(subscriptionRepository).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("키워드 조건이 있으면 houseName LIKE 검색을 수행한다")
        void appliesKeywordCondition() {
            // given
            AdminSubscriptionSearchCriteria criteria = new AdminSubscriptionSearchCriteria(
                    "힐스테이트", null, null, null, null, 0, 20
            );

            when(subscriptionRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(Page.empty());

            // when
            service.search(criteria);

            // then
            verify(subscriptionRepository).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("지역 조건이 있으면 area 필터링을 수행한다")
        void appliesAreaCondition() {
            // given
            AdminSubscriptionSearchCriteria criteria = new AdminSubscriptionSearchCriteria(
                    null, "서울", null, null, null, 0, 20
            );

            when(subscriptionRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(Page.empty());

            // when
            service.search(criteria);

            // then
            verify(subscriptionRepository).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("소스 조건이 있으면 source 필터링을 수행한다")
        void appliesSourceCondition() {
            // given
            AdminSubscriptionSearchCriteria criteria = new AdminSubscriptionSearchCriteria(
                    null, null, "APPLYHOME_API", null, null, 0, 20
            );

            when(subscriptionRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(Page.empty());

            // when
            service.search(criteria);

            // then
            verify(subscriptionRepository).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("날짜 범위 조건이 있으면 receiptStartDate 필터링을 수행한다")
        void appliesDateRangeCondition() {
            // given
            LocalDate startDate = LocalDate.of(2025, 1, 1);
            LocalDate endDate = LocalDate.of(2025, 12, 31);

            AdminSubscriptionSearchCriteria criteria = new AdminSubscriptionSearchCriteria(
                    null, null, null, startDate, endDate, 0, 20
            );

            when(subscriptionRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(Page.empty());

            // when
            service.search(criteria);

            // then
            verify(subscriptionRepository).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("시작일만 있으면 그 이후 데이터만 조회한다")
        void appliesStartDateOnlyCondition() {
            // given
            AdminSubscriptionSearchCriteria criteria = new AdminSubscriptionSearchCriteria(
                    null, null, null, LocalDate.of(2025, 1, 1), null, 0, 20
            );

            when(subscriptionRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(Page.empty());

            // when
            service.search(criteria);

            // then
            verify(subscriptionRepository).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("종료일만 있으면 그 이전 데이터만 조회한다")
        void appliesEndDateOnlyCondition() {
            // given
            AdminSubscriptionSearchCriteria criteria = new AdminSubscriptionSearchCriteria(
                    null, null, null, null, LocalDate.of(2025, 12, 31), 0, 20
            );

            when(subscriptionRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(Page.empty());

            // when
            service.search(criteria);

            // then
            verify(subscriptionRepository).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("모든 조건이 있으면 모두 적용한다")
        void appliesAllConditions() {
            // given
            AdminSubscriptionSearchCriteria criteria = new AdminSubscriptionSearchCriteria(
                    "힐스테이트",
                    "서울",
                    "APPLYHOME_API",
                    LocalDate.of(2025, 1, 1),
                    LocalDate.of(2025, 12, 31),
                    0,
                    20
            );

            when(subscriptionRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(Page.empty());

            // when
            service.search(criteria);

            // then
            verify(subscriptionRepository).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("정렬은 receiptStartDate DESC, createdAt DESC 순이다")
        void sortsResultsByDateDesc() {
            // given
            AdminSubscriptionSearchCriteria criteria = new AdminSubscriptionSearchCriteria(
                    null, null, null, null, null, 0, 20
            );

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

            when(subscriptionRepository.findAll(any(Specification.class), pageableCaptor.capture()))
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
            List<String> areas = List.of("서울", "경기", "인천");
            when(subscriptionRepository.findDistinctAreas()).thenReturn(areas);

            // when
            List<String> result = service.availableAreas();

            // then
            assertThat(result).containsExactly("서울", "경기", "인천");
        }
    }

    @Nested
    @DisplayName("availableSources() - 사용 가능한 소스 목록")
    class AvailableSources {

        @Test
        @DisplayName("저장된 소스 목록을 반환한다")
        void returnsDistinctSources() {
            // given
            List<String> sources = List.of("APPLYHOME_API", "LH_API");
            when(subscriptionRepository.findDistinctSources()).thenReturn(sources);

            // when
            List<String> result = service.availableSources();

            // then
            assertThat(result).containsExactly("APPLYHOME_API", "LH_API");
        }
    }

    private SubscriptionEntity createEntity(String houseName) {
        return SubscriptionEntity.builder()
                .houseName(houseName)
                .area("서울")
                .source("APPLYHOME_API")
                .receiptStartDate(LocalDate.now())
                .build();
    }
}
