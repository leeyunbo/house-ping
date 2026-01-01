package com.yunbok.houseping.infrastructure.adapter.inbound.admin;

import com.yunbok.houseping.infrastructure.persistence.entity.SubscriptionEntity;
import com.yunbok.houseping.infrastructure.persistence.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

/**
 * 관리자 검색 화면을 위한 조회 로직.
 */
@Service
@RequiredArgsConstructor
public class AdminSubscriptionQueryService {

    private final SubscriptionRepository subscriptionRepository;

    public Page<SubscriptionEntity> search(AdminSubscriptionSearchCriteria criteria) {
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

        return subscriptionRepository.findAll(spec, pageRequest);
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
