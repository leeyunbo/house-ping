package com.yunbok.houseping.core.service.subscription;

import com.yunbok.houseping.core.domain.Subscription;
import com.yunbok.houseping.core.port.SubscriptionProvider;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * 여러 Provider를 순차적으로 시도하여 첫 번째 성공 결과를 반환하는 Fallback Chain
 */
@Slf4j
public class FallbackProviderChain implements SubscriptionProviderChain {

    private final List<SubscriptionProvider> providers;
    private final String name;

    public FallbackProviderChain(List<SubscriptionProvider> providers, String name) {
        this.providers = providers;
        this.name = name;
    }

    public List<Subscription> execute(String areaName, LocalDate targetDate) {
        for (SubscriptionProvider provider : providers) {
            Optional<List<Subscription>> result = tryFetch(
                    () -> provider.fetch(areaName, targetDate),
                    provider.getSourceName()
            );
            if (result.isPresent()) {
                return result.get();
            }
        }
        return emptyResultWithWarning();
    }

    public List<Subscription> executeAll(String areaName) {
        for (SubscriptionProvider provider : providers) {
            if (!provider.isExternalSource()) continue;
            Optional<List<Subscription>> result = tryFetch(
                    () -> provider.fetchAll(areaName),
                    provider.getSourceName()
            );
            if (result.isPresent()) {
                return result.get();
            }
        }
        return emptyResultWithWarning();
    }

    public String getSourceName() {
        return name;
    }

    private Optional<List<Subscription>> tryFetch(
            Supplier<List<Subscription>> fetcher, String sourceName) {
        try {
            List<Subscription> result = fetcher.get();
            if (result != null) {
                log.info("[{}] {} succeeded: {} items", name, sourceName, result.size());
                return Optional.of(result);
            }
        } catch (Exception e) {
            log.warn("[{}] {} failed: {}", name, sourceName, e.getMessage());
        }
        return Optional.empty();
    }

    private List<Subscription> emptyResultWithWarning() {
        log.warn("[{}] All sources failed", name);
        return List.of();
    }
}
