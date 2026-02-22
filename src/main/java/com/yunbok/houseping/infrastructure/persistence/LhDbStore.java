package com.yunbok.houseping.infrastructure.persistence;

import com.yunbok.houseping.core.domain.Subscription;
import com.yunbok.houseping.core.domain.SubscriptionSource;
import com.yunbok.houseping.entity.SubscriptionEntity;
import com.yunbok.houseping.repository.SubscriptionRepository;
import com.yunbok.houseping.core.port.SubscriptionProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(name = "feature.subscription.lh-db-enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
public class LhDbStore implements SubscriptionProvider {

    private static final String SOURCE_PREFIX = SubscriptionSource.LH.getValue();
    private final SubscriptionRepository subscriptionRepository;

    @Override
    public boolean isExternalSource() {
        return false;
    }

    public List<Subscription> fetch(String areaName, LocalDate targetDate) {
        try {
            log.info("[LH DB] Fetching area={}", areaName);

            List<SubscriptionEntity> entities = subscriptionRepository
                    .findByAreaAndReceiptStartDateGreaterThanEqual(areaName, LocalDate.now());

            List<Subscription> result = entities.stream()
                    .filter(entity -> entity.getSource().startsWith(SOURCE_PREFIX))
                    .map(this::toDomain)
                    .toList();

            log.info("[LH DB] area={} fetched {} items", areaName, result.size());
            return result;
        } catch (Exception e) {
            log.error("[LH DB] Fetch failed: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private Subscription toDomain(SubscriptionEntity entity) {
        return Subscription.builder()
                .id(entity.getId())
                .source(entity.getSource())
                .houseName(entity.getHouseName())
                .houseType(entity.getHouseType())
                .area(entity.getArea())
                .announceDate(entity.getAnnounceDate())
                .receiptStartDate(entity.getReceiptStartDate())
                .receiptEndDate(entity.getReceiptEndDate())
                .detailUrl(entity.getDetailUrl())
                .build();
    }
}
