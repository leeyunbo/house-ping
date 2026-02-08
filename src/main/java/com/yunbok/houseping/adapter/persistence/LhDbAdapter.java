package com.yunbok.houseping.adapter.persistence;

import com.yunbok.houseping.entity.SubscriptionEntity;
import com.yunbok.houseping.repository.SubscriptionRepository;
import com.yunbok.houseping.adapter.dto.LhSubscriptionInfo;
import com.yunbok.houseping.adapter.dto.SubscriptionInfo;
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
public class LhDbAdapter implements SubscriptionProvider {

    private static final String SOURCE_PREFIX = "LH";
    private final SubscriptionRepository subscriptionRepository;

    public List<SubscriptionInfo> fetch(String areaName, LocalDate targetDate) {
        try {
            log.info("[LH DB] Fetching area={}", areaName);

            List<SubscriptionEntity> entities = subscriptionRepository
                    .findByAreaAndReceiptStartDateGreaterThanEqual(areaName, LocalDate.now());

            List<SubscriptionInfo> result = entities.stream()
                    .filter(entity -> entity.getSource().startsWith(SOURCE_PREFIX))
                    .map(this::toSubscriptionInfo)
                    .toList();

            log.info("[LH DB] area={} fetched {} items", areaName, result.size());
            return result;
        } catch (Exception e) {
            log.error("[LH DB] Fetch failed: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private SubscriptionInfo toSubscriptionInfo(SubscriptionEntity entity) {
        return LhSubscriptionInfo.builder()
                .houseName(entity.getHouseName())
                .houseType(entity.getHouseType())
                .area(entity.getArea())
                .announceDate(entity.getAnnounceDate())
                .receiptEndDate(entity.getReceiptEndDate())
                .detailUrl(entity.getDetailUrl())
                .build();
    }
}
