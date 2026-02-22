package com.yunbok.houseping.core.service.subscription;

import com.yunbok.houseping.core.domain.Subscription;
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

    public List<Subscription> collect(LocalDate targetDate, boolean notify) {
        List<Subscription> subscriptions = subscriptionCollector.collectFromAllAreas(targetDate);

        if (notify) {
            sendNotification(subscriptions);
        }

        return subscriptions;
    }

    private void sendNotification(List<Subscription> subscriptions) {
        if (subscriptions.isEmpty()) {
            notificationSender.sendNoDataNotification();
        } else {
            notificationSender.sendNewSubscriptions(subscriptions);
        }
    }
}
