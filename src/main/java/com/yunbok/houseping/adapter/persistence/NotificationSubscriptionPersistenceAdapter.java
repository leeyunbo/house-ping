package com.yunbok.houseping.adapter.persistence;

import com.yunbok.houseping.support.dto.NotificationTarget;
import com.yunbok.houseping.entity.NotificationSubscriptionEntity;
import com.yunbok.houseping.repository.NotificationSubscriptionRepository;
import com.yunbok.houseping.entity.SubscriptionEntity;
import com.yunbok.houseping.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class NotificationSubscriptionPersistenceAdapter {

    private final NotificationSubscriptionRepository notificationSubscriptionRepository;
    private final SubscriptionRepository subscriptionRepository;

    public List<NotificationTarget> findPendingReceiptStartTargets(LocalDate receiptStartDate) {
        List<NotificationSubscriptionEntity> pendingNotifications =
                notificationSubscriptionRepository.findPendingReceiptStartNotifications();

        if (pendingNotifications.isEmpty()) {
            return List.of();
        }

        Map<Long, SubscriptionEntity> subscriptionMap = getSubscriptionMap(pendingNotifications);

        return pendingNotifications.stream()
                .filter(n -> {
                    SubscriptionEntity sub = subscriptionMap.get(n.getSubscriptionId());
                    return sub != null && receiptStartDate.equals(sub.getReceiptStartDate());
                })
                .map(n -> toNotificationTarget(n, subscriptionMap.get(n.getSubscriptionId())))
                .toList();
    }

    public List<NotificationTarget> findPendingReceiptEndTargets(LocalDate receiptEndDate) {
        List<NotificationSubscriptionEntity> pendingNotifications =
                notificationSubscriptionRepository.findPendingReceiptEndNotifications();

        if (pendingNotifications.isEmpty()) {
            return List.of();
        }

        Map<Long, SubscriptionEntity> subscriptionMap = getSubscriptionMap(pendingNotifications);

        return pendingNotifications.stream()
                .filter(n -> {
                    SubscriptionEntity sub = subscriptionMap.get(n.getSubscriptionId());
                    return sub != null && receiptEndDate.equals(sub.getReceiptEndDate());
                })
                .map(n -> toNotificationTarget(n, subscriptionMap.get(n.getSubscriptionId())))
                .toList();
    }

    public void markReceiptStartNotified(Long notificationId) {
        notificationSubscriptionRepository.findById(notificationId)
                .ifPresent(NotificationSubscriptionEntity::markReceiptStartNotified);
    }

    public void markReceiptEndNotified(Long notificationId) {
        notificationSubscriptionRepository.findById(notificationId)
                .ifPresent(NotificationSubscriptionEntity::markReceiptEndNotified);
    }

    private Map<Long, SubscriptionEntity> getSubscriptionMap(List<NotificationSubscriptionEntity> notifications) {
        List<Long> subscriptionIds = notifications.stream()
                .map(NotificationSubscriptionEntity::getSubscriptionId)
                .toList();

        return subscriptionRepository.findAllById(subscriptionIds)
                .stream()
                .collect(Collectors.toMap(SubscriptionEntity::getId, Function.identity()));
    }

    private NotificationTarget toNotificationTarget(NotificationSubscriptionEntity notification, SubscriptionEntity subscription) {
        return new NotificationTarget(
                notification.getId(),
                subscription.getId(),
                subscription.getHouseName(),
                subscription.getArea(),
                subscription.getReceiptStartDate(),
                subscription.getReceiptEndDate(),
                subscription.getTotalSupplyCount(),
                subscription.getDetailUrl()
        );
    }
}
