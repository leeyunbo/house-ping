package com.yunbok.houseping.domain.service;

import com.yunbok.houseping.domain.model.SubscriptionInfo;
import com.yunbok.houseping.domain.port.outbound.SubscriptionOuterWorldProvider;
import com.yunbok.houseping.infrastructure.adapter.outbound.api.LhApiAdapter;
import com.yunbok.houseping.infrastructure.adapter.outbound.web.LhWebAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * LH 데이터 제공자 Fallback Chain
 * 1차: LhApiAdapter (API 호출)
 * 2차: LhWebAdapter (Web Scraping)
 */
@Slf4j
@Component
@Primary
@ConditionalOnProperty(
        name = "feature.subscription.lh-api-enabled",
        havingValue = "true"
)
@RequiredArgsConstructor
public class FallbackLhOrchestrator implements SubscriptionProviderOrchestrator {

    private final LhApiAdapter lhApiAdapter;
    private final LhWebAdapter lhWebAdapter;

    @Override
    public List<SubscriptionInfo> orchestrate(String areaName, LocalDate targetDate) {
        // 1차: API 시도
        try {
            List<SubscriptionInfo> apiResult = lhApiAdapter.fetch(areaName, targetDate);

            if (apiResult != null && !apiResult.isEmpty()) {
                return apiResult;
            }
        } catch (Exception e) {
            log.error("[LH Fallback] ❌ API 호출 실패: {}, DB Fallback 시도", e.getMessage());
            List<SubscriptionInfo> webResult = fallback(areaName, targetDate);
            if (webResult != null) return webResult;
        }

        return Collections.emptyList();
    }

    private List<SubscriptionInfo> fallback(String areaName, LocalDate targetDate) {
        try {
            log.info("[LH Fallback] Web Scraping 시도");
            List<SubscriptionInfo> webResult = lhWebAdapter.fetch(areaName, targetDate);

            if (webResult != null && !webResult.isEmpty()) {
                return webResult;
            }
        } catch (Exception e) {
            log.error("[LH Fallback] ❌ Web Scraping 실패: {}", e.getMessage());
        }
        return null;
    }
}
