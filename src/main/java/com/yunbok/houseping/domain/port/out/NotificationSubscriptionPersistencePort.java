package com.yunbok.houseping.domain.port.out;

import com.yunbok.houseping.domain.model.NotificationTarget;

import java.time.LocalDate;
import java.util.List;

/**
 * 알림 구독 영속성 포트
 */
public interface NotificationSubscriptionPersistencePort {

    /**
     * 접수 시작 알림 대상 조회 (활성화 + 미발송 + 접수 시작일 일치)
     */
    List<NotificationTarget> findPendingReceiptStartTargets(LocalDate receiptStartDate);

    /**
     * 접수 종료 알림 대상 조회 (활성화 + 미발송 + 접수 종료일 일치)
     */
    List<NotificationTarget> findPendingReceiptEndTargets(LocalDate receiptEndDate);

    /**
     * 접수 시작 알림 발송 완료 처리
     */
    void markReceiptStartNotified(Long notificationId);

    /**
     * 접수 종료 알림 발송 완료 처리
     */
    void markReceiptEndNotified(Long notificationId);
}
