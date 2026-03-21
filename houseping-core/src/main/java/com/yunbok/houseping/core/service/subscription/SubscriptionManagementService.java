package com.yunbok.houseping.core.service.subscription;

import com.yunbok.houseping.core.domain.Subscription;
import com.yunbok.houseping.core.domain.SubscriptionConfig;
import com.yunbok.houseping.core.port.SubscriptionPersistencePort;
import com.yunbok.houseping.support.dto.SyncResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionManagementService {

    private final SubscriptionPersistencePort subscriptionStore;
    private final List<SubscriptionProviderChain> chains;
    private final SubscriptionConfig config;

    public SyncResult sync() {
        SyncResult totalResult = SyncResult.empty();
        for (String area : config.targetAreas()) {
            for (SubscriptionProviderChain chain : chains) {
                List<Subscription> subscriptions = chain.executeAll(area);
                totalResult = totalResult.merge(saveSubscriptions(subscriptions, chain.getSourceName()));
            }
        }
        log.info("[sync] completed: inserted={}, updated={}, skipped={}",
                totalResult.inserted(), totalResult.updated(), totalResult.skipped());
        return totalResult;
    }

    @Transactional
    public int cleanup() {
        LocalDate cutoffDate = LocalDate.now().minusYears(5);
        int deletedCount = subscriptionStore.deleteOldSubscriptions(cutoffDate);
        log.info("[sync.cleanup] completed: deleted={}", deletedCount);
        return deletedCount;
    }

    private SyncResult saveSubscriptions(List<Subscription> subscriptions, String source) {
        int inserted = 0, updated = 0, skipped = 0;
        for (Subscription subscription : subscriptions) {
            try {
                Optional<Subscription> existing = findExisting(subscription, source);
                if (existing.isPresent()) {
                    subscriptionStore.update(subscription, source);
                    updated++;
                } else {
                    subscriptionStore.save(subscription, source);
                    inserted++;
                }
            } catch (Exception e) {
                skipped++;
                log.warn("[sync] 저장 실패 (건너뜀): {} - {}", subscription.getHouseName(), e.getMessage());
            }
        }
        return new SyncResult(inserted, updated, skipped);
    }

    private Optional<Subscription> findExisting(Subscription subscription, String source) {
        String houseManageNo = subscription.getHouseManageNo();
        if (houseManageNo != null && !houseManageNo.isEmpty()) {
            return subscriptionStore.findByHouseManageNo(houseManageNo);
        }
        return subscriptionStore.findBySourceAndHouseNameAndReceiptStartDate(
                source, subscription.getHouseName(), subscription.getReceiptStartDate());
    }
}
