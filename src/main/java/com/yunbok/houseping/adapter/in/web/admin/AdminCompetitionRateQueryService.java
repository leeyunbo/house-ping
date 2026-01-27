package com.yunbok.houseping.adapter.in.web.admin;

import com.yunbok.houseping.infrastructure.persistence.CompetitionRateEntity;
import com.yunbok.houseping.infrastructure.persistence.CompetitionRateRepository;
import com.yunbok.houseping.infrastructure.persistence.SubscriptionEntity;
import com.yunbok.houseping.infrastructure.persistence.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminCompetitionRateQueryService {

    private final CompetitionRateRepository competitionRateRepository;
    private final SubscriptionRepository subscriptionRepository;

    public Page<AdminCompetitionRateDto> search(AdminCompetitionRateSearchCriteria criteria) {
        Specification<CompetitionRateEntity> spec = alwaysTrue();

        if (StringUtils.hasText(criteria.keyword())) {
            spec = spec.and(houseManageNoOrPblancNoLike(criteria.keyword()));
        }
        if (StringUtils.hasText(criteria.houseType())) {
            spec = spec.and(equalsIgnoreCase("houseType", criteria.houseType()));
        }
        if (criteria.rank() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("rank"), criteria.rank()));
        }
        if (StringUtils.hasText(criteria.residenceArea())) {
            spec = spec.and(equalsIgnoreCase("residenceArea", criteria.residenceArea()));
        }
        if (criteria.minRate() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("competitionRate"), criteria.minRate()));
        }
        if (criteria.maxRate() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("competitionRate"), criteria.maxRate()));
        }

        Sort sort = Sort.by(
                Sort.Order.desc("collectedAt"),
                Sort.Order.desc("competitionRate")
        );
        PageRequest pageRequest = PageRequest.of(criteria.page(), criteria.size(), sort);

        Page<CompetitionRateEntity> resultPage = competitionRateRepository.findAll(spec, pageRequest);

        // 청약 정보 조인을 위한 매핑
        Map<String, SubscriptionEntity> subscriptionMap = buildSubscriptionMap(resultPage.getContent());

        return resultPage.map(entity -> toDto(entity, subscriptionMap));
    }

    private Map<String, SubscriptionEntity> buildSubscriptionMap(List<CompetitionRateEntity> competitionRates) {
        List<String> houseManageNos = competitionRates.stream()
                .map(CompetitionRateEntity::getHouseManageNo)
                .distinct()
                .toList();

        // houseManageNo로 청약 정보 조회
        return subscriptionRepository.findAll((root, query, cb) ->
                        root.get("houseManageNo").in(houseManageNos))
                .stream()
                .collect(Collectors.toMap(
                        SubscriptionEntity::getHouseManageNo,
                        entity -> entity,
                        (existing, replacement) -> existing // 중복 시 첫 번째 유지
                ));
    }

    private AdminCompetitionRateDto toDto(CompetitionRateEntity entity, Map<String, SubscriptionEntity> subscriptionMap) {
        SubscriptionEntity subscription = subscriptionMap.get(entity.getHouseManageNo());

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
                subscription != null ? subscription.getHouseName() : null,
                subscription != null ? subscription.getArea() : null
        );
    }

    public List<String> availableHouseTypes() {
        return competitionRateRepository.findDistinctHouseTypes();
    }

    public CompetitionRateStats getStats() {
        List<CompetitionRateEntity> all = competitionRateRepository.findAll();

        if (all.isEmpty()) {
            return new CompetitionRateStats(0, null, null, null);
        }

        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal max = null;
        BigDecimal min = null;
        int count = 0;

        for (CompetitionRateEntity entity : all) {
            BigDecimal rate = entity.getCompetitionRate();
            if (rate != null) {
                sum = sum.add(rate);
                count++;
                if (max == null || rate.compareTo(max) > 0) max = rate;
                if (min == null || rate.compareTo(min) < 0) min = rate;
            }
        }

        BigDecimal avg = count > 0 ? sum.divide(BigDecimal.valueOf(count), 2, java.math.RoundingMode.HALF_UP) : null;

        return new CompetitionRateStats(all.size(), avg, max, min);
    }

    private Specification<CompetitionRateEntity> alwaysTrue() {
        return (root, query, cb) -> cb.conjunction();
    }

    private Specification<CompetitionRateEntity> houseManageNoOrPblancNoLike(String keyword) {
        String likeKeyword = "%" + keyword.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("houseManageNo")), likeKeyword),
                cb.like(cb.lower(root.get("pblancNo")), likeKeyword)
        );
    }

    private Specification<CompetitionRateEntity> equalsIgnoreCase(String field, String value) {
        return (root, query, cb) ->
                cb.equal(cb.lower(root.get(field)), value.trim().toLowerCase());
    }

    public void deleteAll() {
        competitionRateRepository.deleteAll();
    }

    public record CompetitionRateStats(
            int totalCount,
            BigDecimal avgRate,
            BigDecimal maxRate,
            BigDecimal minRate
    ) {}
}
