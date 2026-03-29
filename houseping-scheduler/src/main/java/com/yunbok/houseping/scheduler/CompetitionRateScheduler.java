package com.yunbok.houseping.scheduler;

import com.yunbok.houseping.core.service.competition.CompetitionRateCollectorService;
import com.yunbok.houseping.support.annotation.SchedulerMonitor;
import com.yunbok.houseping.support.dto.SchedulerResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "feature.subscription.applyhome-competition-enabled",
        havingValue = "true"
)
public class CompetitionRateScheduler {

    private final CompetitionRateCollectorService collectorService;

    @SchedulerMonitor("경쟁률 수집")
    @Scheduled(cron = "0 0 10 * * *", zone = "Asia/Seoul")
    public SchedulerResult collectCompetitionRates() {
        int count = collectorService.collect();
        return SchedulerResult.success(count);
    }
}
