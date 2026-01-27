package com.yunbok.houseping.adapter.in.scheduler;

import com.yunbok.houseping.domain.service.CompetitionRateCollectorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 경쟁률 수집 스케줄러
 * 당첨자 발표일이 지난 청약의 경쟁률 수집
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "feature.subscription.applyhome-competition-enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class CompetitionRateScheduler {

    private final CompetitionRateCollectorService collectorService;

    /**
     * 매일 오전 10시에 경쟁률 수집
     */
    @Scheduled(cron = "0 0 10 * * *", zone = "Asia/Seoul")
    public void collectCompetitionRates() {
        log.info("[경쟁률 스케줄러] 실행 시작");
        try {
            int count = collectorService.collect();
            log.info("[경쟁률 스케줄러] 실행 완료 - {}건 수집", count);
        } catch (Exception e) {
            log.error("[경쟁률 스케줄러] 실행 실패", e);
        }
    }
}
