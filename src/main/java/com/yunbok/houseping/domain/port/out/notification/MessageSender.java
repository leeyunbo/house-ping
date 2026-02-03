package com.yunbok.houseping.domain.port.out.notification;

/**
 * 일반 메시지 발송을 위한 포트
 */
public interface MessageSender {

    /**
     * 일반 메시지 발송
     */
    void sendNotification(String message);

    /**
     * 신규 청약 없음 알림 발송
     */
    void sendNoDataNotification();
}
