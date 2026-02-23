package com.yunbok.houseping.infrastructure.persistence;

import com.yunbok.houseping.core.domain.Subscription;
import com.yunbok.houseping.core.port.SubscriptionPersistencePort;
import com.yunbok.houseping.entity.SubscriptionEntity;
import com.yunbok.houseping.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class SubscriptionStore implements SubscriptionPersistencePort {

    private final SubscriptionRepository subscriptionRepository;

    // ── Query ──

    public Optional<Subscription> findById(Long id) {
        return subscriptionRepository.findById(id)
                .map(this::toDomain);
    }

    public List<Subscription> findByAreaContaining(String area) {
        return subscriptionRepository.findByAreaContaining(area).stream()
                .map(this::toDomain)
                .toList();
    }

    public List<Subscription> findBySourceAndAreas(String source, List<String> areas) {
        return subscriptionRepository.findBySourceAndAreaIn(source, areas).stream()
                .map(this::toDomain)
                .toList();
    }

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

    public List<Subscription> findByReceiptStartDateBetween(LocalDate startDate, LocalDate endDate) {
        return subscriptionRepository.findByReceiptStartDateBetween(startDate, endDate).stream()
                .map(this::toDomain)
                .toList();
    }

    public List<Subscription> findByReceiptPeriodOverlapping(LocalDate weekStart, LocalDate weekEnd) {
        return subscriptionRepository.findByReceiptPeriodOverlapping(weekStart, weekEnd).stream()
                .map(this::toDomain)
                .toList();
    }

    public List<Subscription> findAll() {
        return subscriptionRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    public List<Subscription> findRecentSubscriptions(int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "receiptStartDate"));
        return subscriptionRepository.findAll(pageRequest).getContent().stream()
                .map(this::toDomain)
                .toList();
    }

    public Optional<Subscription> findBySourceAndHouseNameAndReceiptStartDate(
            String source, String houseName, LocalDate receiptStartDate) {
        return subscriptionRepository
                .findBySourceAndHouseNameAndReceiptStartDate(source, houseName, receiptStartDate)
                .map(this::toDomain);
    }

    public List<Subscription> findByAreaAndReceiptStartDate(String area, LocalDate receiptStartDate) {
        return subscriptionRepository.findByAreaAndReceiptStartDate(area, receiptStartDate)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    public Set<String> findHouseManageNosByAreas(List<String> areas) {
        if (areas == null || areas.isEmpty()) {
            return Set.of();
        }
        return new HashSet<>(subscriptionRepository.findHouseManageNosByAreaIn(areas));
    }

    // ── Command ──

    public void save(Subscription subscription, String source) {
        SubscriptionEntity entity = toEntity(subscription, source);
        subscriptionRepository.save(entity);
    }

    public void update(Subscription subscription, String source) {
        subscriptionRepository
                .findBySourceAndHouseNameAndReceiptStartDate(
                        source, subscription.getHouseName(), subscription.getReceiptStartDate())
                .ifPresent(existing -> {
                    SubscriptionEntity updated = toEntity(subscription, source);
                    if (existing.needsUpdate(updated)) {
                        existing.updateFrom(updated);
                        subscriptionRepository.save(existing);
                    }
                });
    }

    public int deleteOldSubscriptions(LocalDate cutoffDate) {
        return subscriptionRepository.deleteOldSubscriptions(cutoffDate);
    }

    // ── Mapping ──

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

    private SubscriptionEntity toEntity(Subscription subscription, String source) {
        return SubscriptionEntity.builder()
                .source(source)
                .houseManageNo(subscription.getHouseManageNo())
                .pblancNo(subscription.getPblancNo())
                .houseName(subscription.getHouseName())
                .houseType(subscription.getHouseType())
                .area(subscription.getArea())
                .announceDate(subscription.getAnnounceDate())
                .receiptStartDate(subscription.getReceiptStartDate())
                .receiptEndDate(subscription.getReceiptEndDate())
                .winnerAnnounceDate(subscription.getWinnerAnnounceDate())
                .detailUrl(subscription.getDetailUrl())
                .homepageUrl(subscription.getHomepageUrl())
                .contact(subscription.getContact())
                .totalSupplyCount(subscription.getTotalSupplyCount())
                .address(subscription.getAddress())
                .zipCode(subscription.getZipCode())
                .collectedAt(LocalDateTime.now())
                .build();
    }
}
