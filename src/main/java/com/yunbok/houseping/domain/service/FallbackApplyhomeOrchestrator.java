package com.yunbok.houseping.domain.service;

import com.yunbok.houseping.domain.model.SubscriptionInfo;
import com.yunbok.houseping.infrastructure.adapter.outbound.api.ApplyhomeApiAdapter;
import com.yunbok.houseping.infrastructure.adapter.outbound.db.ApplyhomeDbAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * 청약Home 데이터 제공자 Fallback Chain
 * 1차: ApplyhomeApiAdapter (API 호출)
 * 2차: ApplyhomeDbAdapter (DB 조회)
 */
@Slf4j
@Component
@Primary
@ConditionalOnProperty(
        name = "feature.subscription.applyhome-api-enabled",
        havingValue = "true"
)
@RequiredArgsConstructor
public class FallbackApplyhomeOrchestrator implements SubscriptionProviderOrchestrator {

    private final ApplyhomeApiAdapter applyhomeApiAdapter;
    private final ApplyhomeDbAdapter applyhomeDbAdapter;

    @Override
    public List<SubscriptionInfo> orchestrate(String areaName, LocalDate targetDate) {
        try {
            List<SubscriptionInfo> apiResult = applyhomeApiAdapter.fetch(areaName, targetDate);

            if (apiResult != null && !apiResult.isEmpty()) {
                return apiResult;
            }
        } catch (Exception e) {
            log.error("[청약Home Fallback] ❌ API 호출 실패: {}, DB Fallback 시도", e.getMessage());
            List<SubscriptionInfo> dbResult = fallback(areaName, targetDate);
            if (dbResult != null) return dbResult;
        }

        return Collections.emptyList();
    }

    private List<SubscriptionInfo> fallback(String areaName, LocalDate targetDate) {
        try {
            List<SubscriptionInfo> dbResult = applyhomeDbAdapter.fetch(areaName, targetDate);

            if (dbResult != null && !dbResult.isEmpty()) {
                return dbResult;
            }
        } catch (Exception e) {
            log.error("[청약Home Fallback] ❌ DB 조회 실패: {}", e.getMessage());
        }
        return null;
    }
}
