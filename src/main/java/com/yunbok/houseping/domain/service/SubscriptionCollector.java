package com.yunbok.houseping.domain.service;

import com.yunbok.houseping.domain.model.SubscriptionConfig;
import com.yunbok.houseping.domain.model.SubscriptionInfo;
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

    private final List<SubscriptionProviderOrchestrator> orchestrators;
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
        for (SubscriptionProviderOrchestrator orchestrator : orchestrators) {
            areaSubscriptions.addAll(orchestrator.orchestrate(area, targetDate));
        }
        return areaSubscriptions;
    }
}
