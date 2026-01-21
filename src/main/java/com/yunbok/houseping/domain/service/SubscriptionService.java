package com.yunbok.houseping.domain.service;

import com.yunbok.houseping.domain.model.SubscriptionInfo;
import com.yunbok.houseping.domain.port.in.SubscriptionUseCase;
import com.yunbok.houseping.domain.port.out.notification.NotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService implements SubscriptionUseCase {

    private final SubscriptionCollector subscriptionCollector;
    private final NotificationSender notificationSender;

    @Override
    public List<SubscriptionInfo> collect(LocalDate targetDate, boolean notify) {
        List<SubscriptionInfo> subscriptions = subscriptionCollector.collectFromAllAreas(targetDate);

        if (notify) {
            if (subscriptions.isEmpty()) {
                notificationSender.sendNotification("No new subscriptions for " + targetDate);
            } else {
                notificationSender.sendNewSubscriptions(subscriptions);
            }
        }

        return subscriptions;
    }
}
