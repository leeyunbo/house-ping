package com.yunbok.houseping.scheduler;

import com.yunbok.houseping.infrastructure.api.RealTransactionApiClient;
import com.yunbok.houseping.infrastructure.api.SchedulerErrorSlackClient;
import com.yunbok.houseping.core.domain.SubscriptionSource;
import com.yunbok.houseping.core.service.region.RegionCodeService;
import com.yunbok.houseping.entity.SubscriptionEntity;
import com.yunbok.houseping.repository.SubscriptionRepository;
import com.yunbok.houseping.support.util.ApiRateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 실거래가 데이터 수집 스케줄러
 * 접수예정 청약 지역의 실거래가를 미리 수집하여 캐싱
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RealTransactionScheduler {

    private final SubscriptionRepository subscriptionRepository;
    private final RegionCodeService regionCodeUseCase;
    private final RealTransactionApiClient realTransactionApiAdapter;
    private final SchedulerErrorSlackClient errorNotifier;

    /**
     * 매일 새벽 4시에 접수예정 청약 지역의 실거래가 수집
     */
    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
    public void collectRealTransactions() {
        log.info("[실거래가 스케줄러] 수집 시작");

        try {
            LocalDate today = LocalDate.now();

            // 접수예정 + 접수중 청약 조회 (ApplyHome만)
            List<SubscriptionEntity> subscriptions = subscriptionRepository.findAll().stream()
                    .filter(s -> SubscriptionSource.APPLYHOME.matches(s.getSource()))
                    .filter(s -> s.getReceiptStartDate() != null)
                    .filter(s -> {
                        LocalDate endDate = s.getReceiptEndDate() != null ? s.getReceiptEndDate() : s.getReceiptStartDate();
                        return !endDate.isBefore(today); // 마감 안 된 것만
                    })
                    .toList();

            log.info("[실거래가 스케줄러] 대상 청약: {}건", subscriptions.size());

            // 중복 제거를 위해 법정동코드 Set 사용
            Set<String> lawdCodes = new HashSet<>();

            for (SubscriptionEntity subscription : subscriptions) {
                Optional<String> lawdCd = regionCodeUseCase.findLawdCdByAddress(subscription.getAddress());
                lawdCd.ifPresent(lawdCodes::add);
            }

            log.info("[실거래가 스케줄러] 수집 대상 지역: {}개", lawdCodes.size());

            int successCount = 0;
            int failCount = 0;

            for (String lawdCd : lawdCodes) {
                try {
                    // 6개월치 실거래가 수집 (2개월 전부터)
                    realTransactionApiAdapter.fetchRecentTransactions(lawdCd, 6);
                    successCount++;
                    ApiRateLimiter.delay(200); // API 과부하 방지
                } catch (Exception e) {
                    failCount++;
                    log.warn("[실거래가 스케줄러] {} 수집 실패: {}", lawdCd, e.getMessage());
                }
            }

            log.info("[실거래가 스케줄러] 수집 완료 - 성공: {}개 지역, 실패: {}개 지역", successCount, failCount);
        } catch (Exception e) {
            log.error("[실거래가 스케줄러] 수집 중 오류", e);
            errorNotifier.sendError("실거래가 수집", e);
        }
    }
}
