package com.yunbok.houseping.adapter.in.scheduler;

import com.yunbok.houseping.domain.port.in.SubscriptionManagementUseCase;
import com.yunbok.houseping.domain.port.in.SubscriptionUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class SubscriptionScheduler {

    private final SubscriptionUseCase subscriptionUseCase;
    private final SubscriptionManagementUseCase managementUseCase;

    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    public void syncRecentData() {
        managementUseCase.sync();
    }

    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
    public void collectDailySubscriptions() {
        subscriptionUseCase.collect(LocalDate.now(), true);
    }

    @Scheduled(cron = "0 0 2 1 * *", zone = "Asia/Seoul")
    public void cleanupOldData() {
        managementUseCase.cleanup();
    }
}
