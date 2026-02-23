package com.yunbok.houseping.core.port;

import com.yunbok.houseping.support.dto.NotificationTarget;

import java.time.LocalDate;
import java.util.List;

public interface NotificationSubscriptionPersistencePort {

    List<NotificationTarget> findPendingReceiptStartTargets(LocalDate receiptStartDate);

    List<NotificationTarget> findPendingReceiptEndTargets(LocalDate receiptEndDate);

    void markReceiptStartNotified(Long notificationId);

    void markReceiptEndNotified(Long notificationId);
}
