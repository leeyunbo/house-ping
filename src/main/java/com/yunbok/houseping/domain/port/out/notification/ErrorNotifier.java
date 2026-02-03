package com.yunbok.houseping.domain.port.out.notification;

/**
 * 에러 알림을 위한 포트
 */
public interface ErrorNotifier {

    /**
     * 에러 알림 발송
     */
    void sendErrorNotification(String errorMessage);
}
