package com.yunbok.houseping.adapter.in.web.admin;

import com.yunbok.houseping.infrastructure.persistence.CompetitionRateEntity;
import com.yunbok.houseping.infrastructure.persistence.CompetitionRateRepository;
import com.yunbok.houseping.infrastructure.persistence.SubscriptionEntity;
import com.yunbok.houseping.infrastructure.persistence.SubscriptionRepository;
import com.querydsl.core.types.Predicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("AdminCompetitionRateQueryService - 경쟁률 조회 서비스")
@ExtendWith(MockitoExtension.class)
class AdminCompetitionRateQueryServiceTest {

    @Mock
    private CompetitionRateRepository competitionRateRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    private AdminCompetitionRateQueryService service;

    @BeforeEach
    void setUp() {
        service = new AdminCompetitionRateQueryService(competitionRateRepository, subscriptionRepository);
    }

    @Nested
    @DisplayName("search() - 검색")
    class Search {

        @Test
        @DisplayName("검색 조건 없이 페이징된 결과를 반환한다")
        void returnsPagedResultsWithoutConditions() {
            // given
            AdminCompetitionRateSearchCriteria criteria = new AdminCompetitionRateSearchCriteria(
                    null, null, null, null, null, null, null, null, 0, 20);
            CompetitionRateEntity entity = createCompetitionRateEntity("H001");
            Page<CompetitionRateEntity> page = new PageImpl<>(List.of(entity));

            when(competitionRateRepository.findAll(any(Predicate.class), any(Pageable.class)))
                    .thenReturn(page);
            when(subscriptionRepository.findAll(any(Predicate.class)))
                    .thenReturn(List.of());

            // when
            Page<AdminCompetitionRateDto> result = service.search(criteria);

            // then
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("키워드 조건이 있으면 houseManageNo 또는 pblancNo를 검색한다")
        void appliesKeywordCondition() {
            // given
            AdminCompetitionRateSearchCriteria criteria = new AdminCompetitionRateSearchCriteria(
                    "H001", null, null, null, null, null, null, null, 0, 20);

            when(competitionRateRepository.findAll(any(Predicate.class), any(Pageable.class)))
                    .thenReturn(Page.empty());
            when(subscriptionRepository.findAll(any(Predicate.class)))
                    .thenReturn(List.of());

            // when
            service.search(criteria);

            // then
            verify(competitionRateRepository).findAll(any(Predicate.class), any(Pageable.class));
        }

        @Test
        @DisplayName("주택명 조건이 있으면 청약 정보에서 houseManageNo를 조회한다")
        void appliesHouseNameCondition() {
            // given
            AdminCompetitionRateSearchCriteria criteria = new AdminCompetitionRateSearchCriteria(
                    null, "힐스테이트", null, null, null, null, null, null, 0, 20);

            when(subscriptionRepository.findHouseManageNosByHouseNameContaining("힐스테이트"))
                    .thenReturn(List.of("H001", "H002"));
            when(competitionRateRepository.findAll(any(Predicate.class), any(Pageable.class)))
                    .thenReturn(Page.empty());
            when(subscriptionRepository.findAll(any(Predicate.class)))
                    .thenReturn(List.of());

            // when
            service.search(criteria);

            // then
            verify(subscriptionRepository).findHouseManageNosByHouseNameContaining("힐스테이트");
        }

        @Test
        @DisplayName("주택명으로 검색 결과가 없으면 빈 결과를 반환한다")
        void returnsEmptyWhenNoHouseNameMatch() {
            // given
            AdminCompetitionRateSearchCriteria criteria = new AdminCompetitionRateSearchCriteria(
                    null, "없는아파트", null, null, null, null, null, null, 0, 20);

            when(subscriptionRepository.findHouseManageNosByHouseNameContaining("없는아파트"))
                    .thenReturn(List.of());
            when(competitionRateRepository.findAll(any(Predicate.class), any(Pageable.class)))
                    .thenReturn(Page.empty());
            when(subscriptionRepository.findAll(any(Predicate.class)))
                    .thenReturn(List.of());

            // when
            Page<AdminCompetitionRateDto> result = service.search(criteria);

            // then
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("지역 조건이 있으면 해당 지역의 houseManageNo를 조회한다")
        void appliesAreaCondition() {
            // given
            AdminCompetitionRateSearchCriteria criteria = new AdminCompetitionRateSearchCriteria(
                    null, null, "서울", null, null, null, null, null, 0, 20);

            when(subscriptionRepository.findHouseManageNosByAreaIn(any()))
                    .thenReturn(List.of("H001"));
            when(competitionRateRepository.findAll(any(Predicate.class), any(Pageable.class)))
                    .thenReturn(Page.empty());
            when(subscriptionRepository.findAll(any(Predicate.class)))
                    .thenReturn(List.of());

            // when
            service.search(criteria);

            // then
            verify(subscriptionRepository).findHouseManageNosByAreaIn(any());
        }

        @Test
        @DisplayName("지역으로 검색 결과가 없으면 빈 결과를 반환한다")
        void returnsEmptyWhenNoAreaMatch() {
            // given
            AdminCompetitionRateSearchCriteria criteria = new AdminCompetitionRateSearchCriteria(
                    null, null, "제주", null, null, null, null, null, 0, 20);

            when(subscriptionRepository.findHouseManageNosByAreaIn(any()))
                    .thenReturn(List.of());
            when(competitionRateRepository.findAll(any(Predicate.class), any(Pageable.class)))
                    .thenReturn(Page.empty());
            when(subscriptionRepository.findAll(any(Predicate.class)))
                    .thenReturn(List.of());

            // when
            Page<AdminCompetitionRateDto> result = service.search(criteria);

            // then
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("주택형 조건이 있으면 houseType을 필터링한다")
        void appliesHouseTypeCondition() {
            // given
            AdminCompetitionRateSearchCriteria criteria = new AdminCompetitionRateSearchCriteria(
                    null, null, null, "084T", null, null, null, null, 0, 20);

            when(competitionRateRepository.findAll(any(Predicate.class), any(Pageable.class)))
                    .thenReturn(Page.empty());
            when(subscriptionRepository.findAll(any(Predicate.class)))
                    .thenReturn(List.of());

            // when
            service.search(criteria);

            // then
            verify(competitionRateRepository).findAll(any(Predicate.class), any(Pageable.class));
        }

        @Test
        @DisplayName("순위 조건이 있으면 rank를 필터링한다")
        void appliesRankCondition() {
            // given
            AdminCompetitionRateSearchCriteria criteria = new AdminCompetitionRateSearchCriteria(
                    null, null, null, null, 1, null, null, null, 0, 20);

            when(competitionRateRepository.findAll(any(Predicate.class), any(Pageable.class)))
                    .thenReturn(Page.empty());
            when(subscriptionRepository.findAll(any(Predicate.class)))
                    .thenReturn(List.of());

            // when
            service.search(criteria);

            // then
            verify(competitionRateRepository).findAll(any(Predicate.class), any(Pageable.class));
        }

        @Test
        @DisplayName("거주지역 조건이 있으면 residenceArea를 필터링한다")
        void appliesResidenceAreaCondition() {
            // given
            AdminCompetitionRateSearchCriteria criteria = new AdminCompetitionRateSearchCriteria(
                    null, null, null, null, null, "해당지역", null, null, 0, 20);

            when(competitionRateRepository.findAll(any(Predicate.class), any(Pageable.class)))
                    .thenReturn(Page.empty());
            when(subscriptionRepository.findAll(any(Predicate.class)))
                    .thenReturn(List.of());

            // when
            service.search(criteria);

            // then
            verify(competitionRateRepository).findAll(any(Predicate.class), any(Pageable.class));
        }

        @Test
        @DisplayName("최소 경쟁률 조건이 있으면 minRate로 필터링한다")
        void appliesMinRateCondition() {
            // given
            AdminCompetitionRateSearchCriteria criteria = new AdminCompetitionRateSearchCriteria(
                    null, null, null, null, null, null, new BigDecimal("10.0"), null, 0, 20);

            when(competitionRateRepository.findAll(any(Predicate.class), any(Pageable.class)))
                    .thenReturn(Page.empty());
            when(subscriptionRepository.findAll(any(Predicate.class)))
                    .thenReturn(List.of());

            // when
            service.search(criteria);

            // then
            verify(competitionRateRepository).findAll(any(Predicate.class), any(Pageable.class));
        }

        @Test
        @DisplayName("최대 경쟁률 조건이 있으면 maxRate로 필터링한다")
        void appliesMaxRateCondition() {
            // given
            AdminCompetitionRateSearchCriteria criteria = new AdminCompetitionRateSearchCriteria(
                    null, null, null, null, null, null, null, new BigDecimal("50.0"), 0, 20);

            when(competitionRateRepository.findAll(any(Predicate.class), any(Pageable.class)))
                    .thenReturn(Page.empty());
            when(subscriptionRepository.findAll(any(Predicate.class)))
                    .thenReturn(List.of());

            // when
            service.search(criteria);

            // then
            verify(competitionRateRepository).findAll(any(Predicate.class), any(Pageable.class));
        }

        @Test
        @DisplayName("청약 정보를 조인하여 주택명과 지역을 반환한다")
        void joinsSubscriptionInfo() {
            // given
            AdminCompetitionRateSearchCriteria criteria = new AdminCompetitionRateSearchCriteria(
                    null, null, null, null, null, null, null, null, 0, 20);
            CompetitionRateEntity rateEntity = createCompetitionRateEntity("H001");
            SubscriptionEntity subEntity = createSubscriptionEntity("H001", "테스트 아파트", "서울");

            Page<CompetitionRateEntity> page = new PageImpl<>(List.of(rateEntity));

            when(competitionRateRepository.findAll(any(Predicate.class), any(Pageable.class)))
                    .thenReturn(page);
            when(subscriptionRepository.findAll(any(Predicate.class)))
                    .thenReturn(List.of(subEntity));

            // when
            Page<AdminCompetitionRateDto> result = service.search(criteria);

            // then
            assertThat(result.getContent()).hasSize(1);
            AdminCompetitionRateDto dto = result.getContent().get(0);
            assertThat(dto.houseName()).isEqualTo("테스트 아파트");
            assertThat(dto.area()).isEqualTo("서울");
        }
    }

    @Nested
    @DisplayName("availableHouseTypes() - 주택형 목록")
    class AvailableHouseTypes {

        @Test
        @DisplayName("저장된 주택형 목록을 반환한다")
        void returnsDistinctHouseTypes() {
            // given
            when(competitionRateRepository.findDistinctHouseTypes())
                    .thenReturn(List.of("084T", "059A", "074B"));

            // when
            List<String> result = service.availableHouseTypes();

            // then
            assertThat(result).containsExactly("084T", "059A", "074B");
        }
    }

    @Nested
    @DisplayName("availableAreas() - 지역 목록")
    class AvailableAreas {

        @Test
        @DisplayName("정규화된 지역 목록을 반환한다")
        void returnsNormalizedAreas() {
            // given
            when(subscriptionRepository.findDistinctAreas())
                    .thenReturn(List.of("서울", "서울특별시", "경기", "경기도"));

            // when
            List<String> result = service.availableAreas();

            // then
            assertThat(result).containsExactly("경기", "서울");
        }
    }

    @Nested
    @DisplayName("deleteAll() - 전체 삭제")
    class DeleteAll {

        @Test
        @DisplayName("모든 경쟁률 데이터를 삭제한다")
        void deletesAllData() {
            // when
            service.deleteAll();

            // then
            verify(competitionRateRepository).deleteAll();
        }
    }

    private CompetitionRateEntity createCompetitionRateEntity(String houseManageNo) {
        return CompetitionRateEntity.builder()
                .houseManageNo(houseManageNo)
                .pblancNo("P001")
                .houseType("084T")
                .supplyCount(100)
                .requestCount(500)
                .competitionRate(new BigDecimal("5.0"))
                .residenceArea("해당지역")
                .rank(1)
                .collectedAt(LocalDateTime.now())
                .build();
    }

    private SubscriptionEntity createSubscriptionEntity(String houseManageNo, String houseName, String area) {
        return SubscriptionEntity.builder()
                .houseManageNo(houseManageNo)
                .houseName(houseName)
                .area(area)
                .source("ApplyHome")
                .receiptStartDate(LocalDate.now())
                .collectedAt(LocalDateTime.now())
                .build();
    }
}
