package com.yunbok.houseping.core.service.subscription;

import com.yunbok.houseping.adapter.dto.SubscriptionInfo;
import com.yunbok.houseping.core.port.NotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionCollector subscriptionCollector;
    private final NotificationSender notificationSender;

    public List<SubscriptionInfo> collect(LocalDate targetDate, boolean notify) {
        List<SubscriptionInfo> subscriptions = subscriptionCollector.collectFromAllAreas(targetDate);

        if (notify) {
            if (subscriptions.isEmpty()) {
                notificationSender.sendNoDataNotification();
            } else {
                notificationSender.sendNewSubscriptions(subscriptions);
            }
        }

        return subscriptions;
    }
}
