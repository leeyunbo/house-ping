package com.yunbok.houseping.core.service.subscription;

import com.yunbok.houseping.adapter.dto.SubscriptionInfo;
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

    public List<SubscriptionInfo> execute(String areaName, LocalDate targetDate) {
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
            if (result != null) {
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
