package com.yunbok.houseping.domain.service;

import com.yunbok.houseping.domain.model.SubscriptionInfo;
import com.yunbok.houseping.domain.port.CollectSubscriptionUseCase;
import com.yunbok.houseping.domain.port.outbound.NotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 청약 정보 수집 도메인 서비스 (CollectSubscriptionUseCase 구현)
 * <p>
 * ⭐ 변경 사항:
 * - 단일 SubscriptionDataProvider → List<SubscriptionDataProvider> 변경
 * - 이제 청약Home + LH 모든 데이터 제공자를 자동으로 사용
 * - 기존 로직은 그대로 유지, 단지 여러 데이터 소스를 순회하도록 수정
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionCollectorService implements CollectSubscriptionUseCase {

    private final List<SubscriptionProviderOrchestrator> dataOrchestrator;
    private final NotificationSender notificationSender;

    private static final List<String> TARGET_AREA_NAMES = Arrays.asList("서울", "경기");

    @Override
    public List<SubscriptionInfo> collectAndNotifyTodaySubscriptions() {
        LocalDate today = LocalDate.now();
        log.info("🚀 {}의 신규 청약 정보 수집을 시작합니다. (데이터 소스: {}개)", today, dataOrchestrator.size());

        try {
            List<SubscriptionInfo> newSubscriptions = new ArrayList<>();

            // 각 지역별로 모든 유형의 청약 정보 수집
            for (String areaName : TARGET_AREA_NAMES) {
                newSubscriptions.addAll(collectAllAptTypesFromArea(areaName, today));
            }

            if (newSubscriptions.isEmpty()) {
                log.info("ℹ️ 오늘은 신규 청약 정보가 없습니다.");
                notificationSender.sendNotification("📭 오늘은 신규 청약 정보가 없습니다.");
                return newSubscriptions;
            }

            log.info("📢 {}개의 신규 청약 정보를 등록된 사용자들에게 발송합니다.", newSubscriptions.size());
            notificationSender.sendNewSubscriptions(newSubscriptions);

            log.info("✅ 청약 정보 수집 및 발송이 완료되었습니다.");
            return newSubscriptions;

        } catch (Exception e) {
            log.error("❌ 청약 정보 수집 중 오류가 발생했습니다.", e);
            notificationSender.sendErrorNotification("청약 정보 수집 실패: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * 테스트용: 특정 날짜의 청약 정보 수집 (Slack 발송 안함)
     */
    public List<SubscriptionInfo> collectSubscriptionsForDate(LocalDate targetDate) {
        log.info("🔍 [테스트] {}의 청약 정보 수집을 시작합니다. (데이터 소스: {}개)", targetDate, dataOrchestrator.size());

        List<SubscriptionInfo> newSubscriptions = new ArrayList<>();

        for (String areaName : TARGET_AREA_NAMES) {
            newSubscriptions.addAll(collectAllAptTypesFromArea(areaName, targetDate));
        }

        log.info("✅ [테스트] 총 {}개의 청약 정보를 수집했습니다.", newSubscriptions.size());
        return newSubscriptions;
    }

    /**
     * 특정 지역의 모든 APT 유형 정보 수집
     */
    private List<SubscriptionInfo> collectAllAptTypesFromArea(String areaName, LocalDate targetDate) {
        List<SubscriptionInfo> areaSubscriptions = new ArrayList<>();

        log.info("🏠 {} 지역의 청약 정보를 수집합니다.", areaName);

        for (SubscriptionProviderOrchestrator dataOrchestrator : dataOrchestrator) {
            log.info("{} 데이터 수집 시작", areaName);

            List<SubscriptionInfo> providerSubscriptions = dataOrchestrator.orchestrate(areaName, targetDate);
            areaSubscriptions.addAll(providerSubscriptions);

            log.info("{} 지역에서 {}개의 청약 정보를 수집했습니다.", areaName, providerSubscriptions.size());
        }

        log.info("📊 {} 지역에서 총 {}개의 청약 정보를 수집했습니다.", areaName, areaSubscriptions.size());
        return areaSubscriptions;
    }

}
