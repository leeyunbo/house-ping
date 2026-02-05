package com.yunbok.houseping.adapter.out.persistence;

import com.yunbok.houseping.domain.model.Subscription;
import com.yunbok.houseping.domain.port.out.SubscriptionQueryPort;
import com.yunbok.houseping.infrastructure.persistence.SubscriptionEntity;
import com.yunbok.houseping.infrastructure.persistence.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 청약 조회 어댑터
 * Entity를 Domain Model로 변환
 */
@Component
@RequiredArgsConstructor
public class SubscriptionQueryAdapter implements SubscriptionQueryPort {

    private final SubscriptionRepository subscriptionRepository;

    @Override
    public Optional<Subscription> findById(Long id) {
        return subscriptionRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public List<Subscription> findByAreaContaining(String area) {
        return subscriptionRepository.findByAreaContaining(area).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Subscription> findBySourceAndAreas(String source, List<String> areas) {
        return subscriptionRepository.findBySourceAndAreaIn(source, areas).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Subscription> findBySupportedAreas(List<String> areas) {
        if (areas.size() >= 2) {
            return subscriptionRepository.findByAreaLikeOrAreaLike(areas.get(0), areas.get(1)).stream()
                    .map(this::toDomain)
                    .toList();
        } else if (areas.size() == 1) {
            return subscriptionRepository.findByAreaContaining(areas.get(0)).stream()
                    .map(this::toDomain)
                    .toList();
        }
        return List.of();
    }

    @Override
    public List<Subscription> findRecentSubscriptions(int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "receiptStartDate"));
        return subscriptionRepository.findAll(pageRequest).getContent().stream()
                .map(this::toDomain)
                .toList();
    }

    /**
     * Entity -> Domain Model 변환
     */
    private Subscription toDomain(SubscriptionEntity entity) {
        return Subscription.builder()
                .id(entity.getId())
                .source(entity.getSource())
                .houseManageNo(entity.getHouseManageNo())
                .pblancNo(entity.getPblancNo())
                .houseName(entity.getHouseName())
                .houseType(entity.getHouseType())
                .area(entity.getArea())
                .announceDate(entity.getAnnounceDate())
                .receiptStartDate(entity.getReceiptStartDate())
                .receiptEndDate(entity.getReceiptEndDate())
                .winnerAnnounceDate(entity.getWinnerAnnounceDate())
                .detailUrl(entity.getDetailUrl())
                .homepageUrl(entity.getHomepageUrl())
                .contact(entity.getContact())
                .totalSupplyCount(entity.getTotalSupplyCount())
                .address(entity.getAddress())
                .zipCode(entity.getZipCode())
                .build();
    }
}
