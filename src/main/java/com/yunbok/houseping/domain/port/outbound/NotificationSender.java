package com.yunbok.houseping.domain.port.outbound;

import com.yunbok.houseping.domain.model.SubscriptionInfo;

import java.util.List;

/**
 * 알림 발송을 위한 아웃바운드 포트
 */
public interface NotificationSender {
    
    /**
     * 청약 정보를 텔레그램으로 발송
     */
    void sendNewSubscriptions(List<SubscriptionInfo> subscriptions);
    
    /**
     * 단일 청약 정보 발송
     */
    void sendSubscription(SubscriptionInfo subscription);
    
    /**
     * 에러 알림 발송
     */
    void sendErrorNotification(String errorMessage);

    /**
     * 메시지 전송
     */
    void sendNotification(String message);
}
