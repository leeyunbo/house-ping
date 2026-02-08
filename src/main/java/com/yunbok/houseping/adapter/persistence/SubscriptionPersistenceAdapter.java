package com.yunbok.houseping.adapter.persistence;

import com.yunbok.houseping.adapter.dto.SubscriptionInfo;
import com.yunbok.houseping.entity.SubscriptionEntity;
import com.yunbok.houseping.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class SubscriptionPersistenceAdapter {

    private final SubscriptionRepository subscriptionRepository;

    public Optional<SubscriptionInfo> findBySourceAndHouseNameAndReceiptStartDate(
            String source, String houseName, LocalDate receiptStartDate) {
        return subscriptionRepository
                .findBySourceAndHouseNameAndReceiptStartDate(source, houseName, receiptStartDate)
                .map(this::toSubscriptionInfo);
    }

    public void save(SubscriptionInfo info, String source) {
        SubscriptionEntity entity = toEntity(info, source);
        subscriptionRepository.save(entity);
    }

    public void update(SubscriptionInfo info, String source) {
        subscriptionRepository
                .findBySourceAndHouseNameAndReceiptStartDate(
                        source, info.getHouseName(), info.getReceiptStartDate())
                .ifPresent(existing -> {
                    SubscriptionEntity updated = toEntity(info, source);
                    if (existing.needsUpdate(updated)) {
                        existing.updateFrom(updated);
                        subscriptionRepository.save(existing);
                    }
                });
    }

    public int deleteOldSubscriptions(LocalDate cutoffDate) {
        return subscriptionRepository.deleteOldSubscriptions(cutoffDate);
    }

    public Set<String> findHouseManageNosByAreas(List<String> areas) {
        if (areas == null || areas.isEmpty()) {
            return Set.of();
        }
        return new HashSet<>(subscriptionRepository.findHouseManageNosByAreaIn(areas));
    }

    private SubscriptionInfo toSubscriptionInfo(SubscriptionEntity entity) {
        return new SubscriptionInfo() {
            @Override public String getDisplayMessage() { return entity.getHouseName(); }
            @Override public String getSimpleDisplayMessage() { return entity.getHouseName(); }
            @Override public String getHouseName() { return entity.getHouseName(); }
            @Override public String getHouseType() { return entity.getHouseType(); }
            @Override public String getArea() { return entity.getArea(); }
            @Override public LocalDate getAnnounceDate() { return entity.getAnnounceDate(); }
            @Override public LocalDate getReceiptStartDate() { return entity.getReceiptStartDate(); }
            @Override public LocalDate getReceiptEndDate() { return entity.getReceiptEndDate(); }
            @Override public LocalDate getWinnerAnnounceDate() { return entity.getWinnerAnnounceDate(); }
            @Override public String getDetailUrl() { return entity.getDetailUrl(); }
            @Override public String getHomepageUrl() { return entity.getHomepageUrl(); }
            @Override public String getContact() { return entity.getContact(); }
            @Override public Integer getTotalSupplyCount() { return entity.getTotalSupplyCount(); }
            @Override public String getHouseManageNo() { return entity.getHouseManageNo(); }
            @Override public String getPblancNo() { return entity.getPblancNo(); }
            @Override public String getAddress() { return entity.getAddress(); }
            @Override public String getZipCode() { return entity.getZipCode(); }
        };
    }

    private SubscriptionEntity toEntity(SubscriptionInfo info, String source) {
        return SubscriptionEntity.builder()
                .source(source)
                .houseManageNo(info.getHouseManageNo())
                .pblancNo(info.getPblancNo())
                .houseName(info.getHouseName())
                .houseType(info.getHouseType())
                .area(info.getArea())
                .announceDate(info.getAnnounceDate())
                .receiptStartDate(info.getReceiptStartDate())
                .receiptEndDate(info.getReceiptEndDate())
                .winnerAnnounceDate(info.getWinnerAnnounceDate())
                .detailUrl(info.getDetailUrl())
                .homepageUrl(info.getHomepageUrl())
                .contact(info.getContact())
                .totalSupplyCount(info.getTotalSupplyCount())
                .address(info.getAddress())
                .zipCode(info.getZipCode())
                .collectedAt(LocalDateTime.now())
                .build();
    }
}
