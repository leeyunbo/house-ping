package com.yunbok.houseping.service;

import com.yunbok.houseping.service.dto.AdminCompetitionRateDto;
import com.yunbok.houseping.service.dto.AdminCompetitionRateSearchCriteria;
import com.yunbok.houseping.support.util.AreaNormalizer;
import com.yunbok.houseping.entity.CompetitionRateEntity;
import com.yunbok.houseping.repository.CompetitionRateRepository;
import com.yunbok.houseping.entity.QCompetitionRateEntity;
import com.yunbok.houseping.entity.SubscriptionEntity;
import com.yunbok.houseping.repository.SubscriptionRepository;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminCompetitionRateService {

    private final CompetitionRateRepository competitionRateRepository;
    private final SubscriptionRepository subscriptionRepository;

    private static final QCompetitionRateEntity competitionRate = QCompetitionRateEntity.competitionRateEntity;

    @Transactional(readOnly = true)
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
            builder.and(competitionRate.subscription.houseName.containsIgnoreCase(criteria.houseName().trim()));
        }
        if (StringUtils.hasText(criteria.area())) {
            List<String> areas = AreaNormalizer.expand(criteria.area().trim());
            builder.and(competitionRate.subscription.area.in(areas));
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

        return resultPage.map(this::toDto);
    }

    private AdminCompetitionRateDto toDto(CompetitionRateEntity entity) {
        SubscriptionEntity sub = entity.getSubscription();

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
