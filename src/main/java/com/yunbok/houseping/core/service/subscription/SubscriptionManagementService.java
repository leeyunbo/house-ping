package com.yunbok.houseping.core.service.subscription;

import com.yunbok.houseping.core.domain.SubscriptionConfig;
import com.yunbok.houseping.adapter.dto.SubscriptionInfo;
import com.yunbok.houseping.support.dto.SyncResult;
import com.yunbok.houseping.adapter.persistence.SubscriptionPersistenceAdapter;
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

    private final SubscriptionPersistenceAdapter persistencePort;
    private final List<SubscriptionProviderChain> chains;
    private final SubscriptionConfig config;

    @Transactional
    public SyncResult sync() {
        SyncResult totalResult = SyncResult.empty();
        for (String area : config.targetAreas()) {
            for (SubscriptionProviderChain chain : chains) {
                List<SubscriptionInfo> subscriptions = chain.executeAll(area);
                totalResult = totalResult.merge(saveSubscriptions(subscriptions, chain.getSourceName()));
            }
        }
        log.info("Sync completed: inserted={}, updated={}, skipped={}",
                totalResult.inserted(), totalResult.updated(), totalResult.skipped());
        return totalResult;
    }

    @Transactional
    public int cleanup() {
        LocalDate cutoffDate = LocalDate.now().minusYears(5);
        int deletedCount = persistencePort.deleteOldSubscriptions(cutoffDate);
        log.info("Cleanup completed: deleted={}", deletedCount);
        return deletedCount;
    }

    private SyncResult saveSubscriptions(List<SubscriptionInfo> subscriptions, String source) {
        int inserted = 0, updated = 0;
        for (SubscriptionInfo info : subscriptions) {
            Optional<SubscriptionInfo> existing = persistencePort
                    .findBySourceAndHouseNameAndReceiptStartDate(source, info.getHouseName(), info.getReceiptStartDate());
            if (existing.isPresent()) {
                persistencePort.update(info, source);
                updated++;
            } else {
                persistencePort.save(info, source);
                inserted++;
            }
        }
        return new SyncResult(inserted, updated, 0);
    }
}
