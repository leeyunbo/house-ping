package com.yunbok.houseping.domain.service;

import com.yunbok.houseping.domain.model.SubscriptionInfo;
import com.yunbok.houseping.domain.port.out.SubscriptionProvider;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * 여러 Provider를 순차적으로 시도하여 첫 번째 성공 결과를 반환하는 Fallback Orchestrator
 */
@Slf4j
public class FallbackOrchestrator implements SubscriptionProviderOrchestrator {

    private final List<SubscriptionProvider> providers;
    private final String name;

    public FallbackOrchestrator(List<SubscriptionProvider> providers, String name) {
        this.providers = providers;
        this.name = name;
    }

    @Override
    public List<SubscriptionInfo> orchestrate(String areaName, LocalDate targetDate) {
        for (SubscriptionProvider provider : providers) {
            Optional<List<SubscriptionInfo>> result = tryFetch(
                    () -> provider.fetch(areaName, targetDate),
                    provider.getSourceName()
            );
            if (result.isPresent()) {
                return result.get();
            }
        }
        return emptyResultWithWarning();
    }

    private Optional<List<SubscriptionInfo>> tryFetch(
            Supplier<List<SubscriptionInfo>> fetcher, String sourceName) {
        try {
            List<SubscriptionInfo> result = fetcher.get();
            if (result != null && !result.isEmpty()) {
                log.info("[{}] {} succeeded: {} items", name, sourceName, result.size());
                return Optional.of(result);
            }
        } catch (Exception e) {
            log.warn("[{}] {} failed: {}", name, sourceName, e.getMessage());
        }
        return Optional.empty();
    }

    private List<SubscriptionInfo> emptyResultWithWarning() {
        log.warn("[{}] All sources failed", name);
        return List.of();
    }
}
