package com.yunbok.houseping.domain.service;

import com.yunbok.houseping.domain.model.NotificationTarget;
import com.yunbok.houseping.domain.port.in.SubscriptionNotificationUseCase;
import com.yunbok.houseping.domain.port.out.NotificationSubscriptionPersistencePort;
import com.yunbok.houseping.domain.port.out.notification.NotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionNotificationService implements SubscriptionNotificationUseCase {

    private final NotificationSubscriptionPersistencePort persistencePort;
    private final Optional<NotificationSender> notificationSender;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy년 M월 d일");

    @Override
    @Transactional
    public int sendScheduledNotifications() {
        if (notificationSender.isEmpty()) {
            log.debug("[알림 서비스] 알림 발송기가 비활성화 상태입니다.");
            return 0;
        }

        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        int receiptStartCount = sendReceiptStartNotifications(tomorrow);
        int receiptEndCount = sendReceiptEndNotifications(today);

        return receiptStartCount + receiptEndCount;
    }

    private int sendReceiptStartNotifications(LocalDate targetDate) {
        List<NotificationTarget> targets = persistencePort.findPendingReceiptStartTargets(targetDate);

        if (targets.isEmpty()) {
            return 0;
        }

        int sentCount = 0;
        for (NotificationTarget target : targets) {
            sendReceiptStartMessage(target);
            persistencePort.markReceiptStartNotified(target.notificationId());
            sentCount++;
        }

        log.info("[알림 서비스] 접수 시작 알림 {}건 발송", sentCount);
        return sentCount;
    }

    private int sendReceiptEndNotifications(LocalDate targetDate) {
        List<NotificationTarget> targets = persistencePort.findPendingReceiptEndTargets(targetDate);

        if (targets.isEmpty()) {
            return 0;
        }

        int sentCount = 0;
        for (NotificationTarget target : targets) {
            sendReceiptEndMessage(target);
            persistencePort.markReceiptEndNotified(target.notificationId());
            sentCount++;
        }

        log.info("[알림 서비스] 접수 종료 알림 {}건 발송", sentCount);
        return sentCount;
    }

    private void sendReceiptStartMessage(NotificationTarget target) {
        String message = String.format("""
                :bell: *내일 접수 시작 알림*

                *%s*
                :round_pushpin: 지역: %s
                :calendar: 접수 기간: %s ~ %s
                :house: 공급 세대: %s세대
                %s
                """,
                target.houseName(),
                target.area() != null ? target.area() : "-",
                formatDate(target.receiptStartDate()),
                formatDate(target.receiptEndDate()),
                target.totalSupplyCount() != null ? target.totalSupplyCount() : "-",
                target.detailUrl() != null ? ":link: " + target.detailUrl() : ""
        );

        notificationSender.ifPresent(sender -> sender.sendNotification(message));
    }

    private void sendReceiptEndMessage(NotificationTarget target) {
        String message = String.format("""
                :warning: *오늘 접수 마감 알림*

                *%s*
                :round_pushpin: 지역: %s
                :calendar: 접수 마감: 오늘 (%s)
                :house: 공급 세대: %s세대
                %s
                """,
                target.houseName(),
                target.area() != null ? target.area() : "-",
                formatDate(target.receiptEndDate()),
                target.totalSupplyCount() != null ? target.totalSupplyCount() : "-",
                target.detailUrl() != null ? ":link: " + target.detailUrl() : ""
        );

        notificationSender.ifPresent(sender -> sender.sendNotification(message));
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMAT) : "-";
    }
}
