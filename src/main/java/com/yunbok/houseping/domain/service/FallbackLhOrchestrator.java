package com.yunbok.houseping.domain.service;

import com.yunbok.houseping.domain.model.SubscriptionInfo;
import com.yunbok.houseping.infrastructure.adapter.outbound.api.LhApiAdapter;
import com.yunbok.houseping.infrastructure.adapter.outbound.db.LhDbAdapter;
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
 * 1차: LhApiAdapter (공공데이터 API)
 * 2차: LhWebAdapter (웹 캘린더 API)
 * 3차: LhDbAdapter (로컬 DB)
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
    private final LhDbAdapter lhDbAdapter;

    @Override
    public List<SubscriptionInfo> orchestrate(String areaName, LocalDate targetDate) {
        // 1차: API 시도
        try {
            log.info("[LH Fallback] 1차 시도: 공공데이터 API");
            List<SubscriptionInfo> apiResult = lhApiAdapter.fetch(areaName, targetDate);

            if (apiResult != null && !apiResult.isEmpty()) {
                log.info("[LH Fallback] ✅ API 성공: {}건", apiResult.size());
                return apiResult;
            }
        } catch (Exception e) {
            log.warn("[LH Fallback] ❌ API 실패: {}", e.getMessage());
        }

        // 2차: Web 시도
        try {
            log.info("[LH Fallback] 2차 시도: 웹 캘린더 API");
            List<SubscriptionInfo> webResult = lhWebAdapter.fetch(areaName, targetDate);

            if (webResult != null && !webResult.isEmpty()) {
                log.info("[LH Fallback] ✅ Web 성공: {}건", webResult.size());
                return webResult;
            }
        } catch (Exception e) {
            log.warn("[LH Fallback] ❌ Web 실패: {}", e.getMessage());
        }

        // 3차: DB 시도
        try {
            log.info("[LH Fallback] 3차 시도: 로컬 DB");
            List<SubscriptionInfo> dbResult = lhDbAdapter.fetch(areaName, targetDate);

            if (dbResult != null && !dbResult.isEmpty()) {
                log.info("[LH Fallback] ✅ DB 성공: {}건", dbResult.size());
                return dbResult;
            }
        } catch (Exception e) {
            log.error("[LH Fallback] ❌ DB 실패: {}", e.getMessage());
        }

        log.warn("[LH Fallback] 모든 소스 실패, 빈 결과 반환");
        return Collections.emptyList();
    }
}
