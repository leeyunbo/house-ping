package com.yunbok.houseping.domain.port.in;

/**
 * 청약 알림 발송 유스케이스
 */
public interface SubscriptionNotificationUseCase {

    /**
     * 예정된 알림 발송
     * - 접수 시작 1일 전 알림
     * - 접수 종료 당일 알림
     *
     * @return 발송된 알림 건수
     */
    int sendScheduledNotifications();
}
