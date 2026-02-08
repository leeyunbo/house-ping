package com.yunbok.houseping.core.service.subscription;

import com.yunbok.houseping.core.domain.SubscriptionConfig;
import com.yunbok.houseping.adapter.dto.SubscriptionInfo;
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

    private final List<SubscriptionProviderChain> chains;
    private final SubscriptionConfig config;

    public List<SubscriptionInfo> collectFromAllAreas(LocalDate targetDate) {
        List<SubscriptionInfo> allSubscriptions = new ArrayList<>();
        for (String area : config.targetAreas()) {
            allSubscriptions.addAll(collectFromArea(area, targetDate));
        }
        log.info("Collected {} subscriptions for date={}", allSubscriptions.size(), targetDate);
        return allSubscriptions;
    }

    public List<SubscriptionInfo> collectFromArea(String area, LocalDate targetDate) {
        List<SubscriptionInfo> areaSubscriptions = new ArrayList<>();
        for (SubscriptionProviderChain chain : chains) {
            areaSubscriptions.addAll(chain.execute(area, targetDate));
        }
        return areaSubscriptions;
    }
}
