package com.yunbok.houseping.adapter.in.web.admin;

import com.yunbok.houseping.infrastructure.persistence.SubscriptionEntity;
import com.yunbok.houseping.infrastructure.persistence.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminSubscriptionQueryService {

    private final SubscriptionRepository subscriptionRepository;

    public Page<AdminSubscriptionDto> search(AdminSubscriptionSearchCriteria criteria) {
        Specification<SubscriptionEntity> spec = alwaysTrue();

        if (StringUtils.hasText(criteria.keyword())) {
            spec = spec.and(houseNameLike(criteria.keyword()));
        }
        if (StringUtils.hasText(criteria.area())) {
            spec = spec.and(equalsIgnoreCase("area", criteria.area()));
        }
        if (StringUtils.hasText(criteria.source())) {
            spec = spec.and(equalsIgnoreCase("source", criteria.source()));
        }
        if (criteria.startDate() != null || criteria.endDate() != null) {
            spec = spec.and(receiptBetween(criteria.startDate(), criteria.endDate()));
        }

        Sort sort = Sort.by(
                Sort.Order.desc("receiptStartDate"),
                Sort.Order.desc("createdAt")
        );
        PageRequest pageRequest = PageRequest.of(criteria.page(), criteria.size(), sort);

        return subscriptionRepository.findAll(spec, pageRequest).map(this::toDto);
    }

    private AdminSubscriptionDto toDto(SubscriptionEntity entity) {
        return new AdminSubscriptionDto(
                entity.getId(),
                entity.getSource(),
                entity.getHouseName(),
                entity.getHouseType(),
                entity.getArea(),
                entity.getAnnounceDate(),
                entity.getReceiptStartDate(),
                entity.getReceiptEndDate(),
                entity.getWinnerAnnounceDate(),
                entity.getDetailUrl(),
                entity.getHomepageUrl(),
                entity.getContact(),
                entity.getTotalSupplyCount(),
                entity.getCollectedAt(),
                entity.getCreatedAt()
        );
    }

    private Specification<SubscriptionEntity> alwaysTrue() {
        return (root, query, cb) -> cb.conjunction();
    }

    public List<String> availableAreas() {
        return subscriptionRepository.findDistinctAreas();
    }

    public List<String> availableSources() {
        return subscriptionRepository.findDistinctSources();
    }

    private Specification<SubscriptionEntity> houseNameLike(String keyword) {
        String likeKeyword = "%" + keyword.trim().toLowerCase() + "%";
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("houseName")), likeKeyword);
    }

    private Specification<SubscriptionEntity> equalsIgnoreCase(String field, String value) {
        return (root, query, cb) ->
                cb.equal(cb.lower(root.get(field)), value.trim().toLowerCase());
    }

    private Specification<SubscriptionEntity> receiptBetween(LocalDate start, LocalDate end) {
        return (root, query, cb) -> {
            if (start != null && end != null) {
                return cb.between(root.get("receiptStartDate"), start, end);
            } else if (start != null) {
                return cb.greaterThanOrEqualTo(root.get("receiptStartDate"), start);
            } else if (end != null) {
                return cb.lessThanOrEqualTo(root.get("receiptStartDate"), end);
            }
            return cb.conjunction();
        };
    }
}
