package com.yunbok.houseping.adapter.in.scheduler;

import com.yunbok.houseping.adapter.out.api.ApplyhomeApiAdapter;
import com.yunbok.houseping.domain.port.in.SubscriptionManagementUseCase;
import com.yunbok.houseping.domain.port.in.SubscriptionUseCase;
import com.yunbok.houseping.infrastructure.persistence.SubscriptionEntity;
import com.yunbok.houseping.infrastructure.persistence.SubscriptionPriceRepository;
import com.yunbok.houseping.infrastructure.persistence.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionScheduler {

    private final SubscriptionUseCase subscriptionUseCase;
    private final SubscriptionManagementUseCase managementUseCase;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPriceRepository priceRepository;
    private final ApplyhomeApiAdapter applyhomeApiAdapter;

    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    public void syncRecentData() {
        // 1단계: 청약 데이터 동기화
        managementUseCase.sync();

        // 2단계: 신규 청약 분양가 수집
        collectPriceData();
    }

    /**
     * ApplyHome 분양가 수집
     * sync 이후 호출되어 신규 청약의 분양가 데이터 수집
     */
    public void collectPriceData() {
        log.info("[스케줄러] ApplyHome 분양가 수집 시작");

        try {
            // 분양가 데이터가 없는 ApplyHome 청약 조회
            List<SubscriptionEntity> subscriptions = subscriptionRepository.findAll().stream()
                    .filter(s -> "ApplyHome".equals(s.getSource()))
                    .filter(s -> s.getHouseManageNo() != null && !s.getHouseManageNo().isEmpty())
                    .filter(s -> !priceRepository.existsByHouseManageNo(s.getHouseManageNo()))
                    .toList();

            log.info("[스케줄러] 분양가 수집 대상: {}건", subscriptions.size());

            int successCount = 0;
            int failCount = 0;

            for (SubscriptionEntity subscription : subscriptions) {
                try {
                    applyhomeApiAdapter.fetchAndSavePriceDetails(
                            subscription.getHouseManageNo(),
                            subscription.getPblancNo(),
                            subscription.getHouseType()
                    );
                    successCount++;
                    Thread.sleep(100); // API 과부하 방지
                } catch (Exception e) {
                    failCount++;
                    log.warn("[분양가] {} 수집 실패: {}", subscription.getHouseName(), e.getMessage());
                }
            }

            log.info("[스케줄러] 분양가 수집 완료 - 성공: {}건, 실패: {}건", successCount, failCount);
        } catch (Exception e) {
            log.error("[스케줄러] 분양가 수집 중 오류", e);
        }
    }

    // 알림은 DailyNotificationScheduler에서 통합 발송
    // @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
    // public void collectDailySubscriptions() {
    //     subscriptionUseCase.collect(LocalDate.now(), false);
    // }

    @Scheduled(cron = "0 0 2 1 * *", zone = "Asia/Seoul")
    public void cleanupOldData() {
        managementUseCase.cleanup();
    }
}
