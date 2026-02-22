package com.yunbok.houseping.core.service.subscription;

import com.yunbok.houseping.core.domain.Subscription;
import com.yunbok.houseping.core.domain.SubscriptionConfig;
import com.yunbok.houseping.infrastructure.persistence.SubscriptionStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionCollector {

    private final SubscriptionStore subscriptionStore;
    private final SubscriptionConfig config;

    public List<Subscription> collectFromAllAreas(LocalDate targetDate) {
        List<Subscription> allSubscriptions = new ArrayList<>();
        for (String area : config.targetAreas()) {
            allSubscriptions.addAll(collectFromArea(area, targetDate));
        }
        log.info("Collected {} subscriptions for date={}", allSubscriptions.size(), targetDate);
        return allSubscriptions;
    }

    public List<Subscription> collectFromArea(String area, LocalDate targetDate) {
        return subscriptionStore.findByAreaAndReceiptStartDate(area, targetDate);
    }
}
