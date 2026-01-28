package com.yunbok.houseping.adapter.in.web.admin;

import com.yunbok.houseping.domain.model.AreaNormalizer;
import com.yunbok.houseping.infrastructure.persistence.CompetitionRateEntity;
import com.yunbok.houseping.infrastructure.persistence.CompetitionRateRepository;
import com.yunbok.houseping.infrastructure.persistence.QCompetitionRateEntity;
import com.yunbok.houseping.infrastructure.persistence.QSubscriptionEntity;
import com.yunbok.houseping.infrastructure.persistence.SubscriptionEntity;
import com.yunbok.houseping.infrastructure.persistence.SubscriptionRepository;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class AdminCompetitionRateQueryService {

    private final CompetitionRateRepository competitionRateRepository;
    private final SubscriptionRepository subscriptionRepository;

    private static final QCompetitionRateEntity competitionRate = QCompetitionRateEntity.competitionRateEntity;
    private static final QSubscriptionEntity subscription = QSubscriptionEntity.subscriptionEntity;

    public Page<AdminCompetitionRateDto> search(AdminCompetitionRateSearchCriteria criteria) {
        BooleanBuilder builder = new BooleanBuilder();

        // 경쟁률이 없는 데이터 제외
        builder.and(competitionRate.competitionRate.isNotNull());

        if (StringUtils.hasText(criteria.keyword())) {
            String keyword = criteria.keyword().trim().toLowerCase();
            builder.and(
                    competitionRate.houseManageNo.containsIgnoreCase(keyword)
                            .or(competitionRate.pblancNo.containsIgnoreCase(keyword))
            );
        }
        if (StringUtils.hasText(criteria.houseName())) {
            List<String> houseManageNos = subscriptionRepository.findHouseManageNosByHouseNameContaining(criteria.houseName().trim());
            if (houseManageNos.isEmpty()) {
                builder.and(competitionRate.houseManageNo.isNull()); // 결과 없음
            } else {
                builder.and(competitionRate.houseManageNo.in(houseManageNos));
            }
        }
        if (StringUtils.hasText(criteria.area())) {
            List<String> houseManageNos = subscriptionRepository.findHouseManageNosByAreaIn(AreaNormalizer.expand(criteria.area().trim()));
            if (houseManageNos.isEmpty()) {
                builder.and(competitionRate.houseManageNo.isNull()); // 결과 없음
            } else {
                builder.and(competitionRate.houseManageNo.in(houseManageNos));
            }
        }
        if (StringUtils.hasText(criteria.houseType())) {
            builder.and(competitionRate.houseType.equalsIgnoreCase(criteria.houseType().trim()));
        }
        if (criteria.rank() != null) {
            builder.and(competitionRate.rank.eq(criteria.rank()));
        }
        if (StringUtils.hasText(criteria.residenceArea())) {
            builder.and(competitionRate.residenceArea.equalsIgnoreCase(criteria.residenceArea().trim()));
        }
        if (criteria.minRate() != null) {
            builder.and(competitionRate.competitionRate.goe(criteria.minRate()));
        }
        if (criteria.maxRate() != null) {
            builder.and(competitionRate.competitionRate.loe(criteria.maxRate()));
        }

        Sort sort = Sort.by(
                Sort.Order.desc("collectedAt"),
                Sort.Order.desc("competitionRate")
        );
        PageRequest pageRequest = PageRequest.of(criteria.page(), criteria.size(), sort);

        Page<CompetitionRateEntity> resultPage = competitionRateRepository.findAll(builder, pageRequest);

        // 청약 정보 조인을 위한 매핑
        Map<String, SubscriptionEntity> subscriptionMap = buildSubscriptionMap(resultPage.getContent());

        return resultPage.map(entity -> toDto(entity, subscriptionMap));
    }

    private Map<String, SubscriptionEntity> buildSubscriptionMap(List<CompetitionRateEntity> competitionRates) {
        List<String> houseManageNos = competitionRates.stream()
                .map(CompetitionRateEntity::getHouseManageNo)
                .distinct()
                .toList();

        return StreamSupport
                .stream(subscriptionRepository.findAll(subscription.houseManageNo.in(houseManageNos)).spliterator(), false)
                .collect(Collectors.toMap(
                        SubscriptionEntity::getHouseManageNo,
                        entity -> entity,
                        (existing, replacement) -> existing // 중복 시 첫 번째 유지
                ));
    }

    private AdminCompetitionRateDto toDto(CompetitionRateEntity entity, Map<String, SubscriptionEntity> subscriptionMap) {
        SubscriptionEntity sub = subscriptionMap.get(entity.getHouseManageNo());

        return new AdminCompetitionRateDto(
                entity.getId(),
                entity.getHouseManageNo(),
                entity.getPblancNo(),
                entity.getHouseType(),
                entity.getSupplyCount(),
                entity.getRequestCount(),
                entity.getCompetitionRate(),
                entity.getResidenceArea(),
                entity.getRank(),
                entity.getCollectedAt(),
                sub != null ? sub.getHouseName() : null,
                sub != null ? sub.getArea() : null
        );
    }

    public List<String> availableHouseTypes() {
        return competitionRateRepository.findDistinctHouseTypes();
    }

    public List<String> availableAreas() {
        return subscriptionRepository.findDistinctAreas().stream()
                .map(AreaNormalizer::normalize)
                .distinct()
                .sorted()
                .toList();
    }

    public void deleteAll() {
        competitionRateRepository.deleteAll();
    }
}
