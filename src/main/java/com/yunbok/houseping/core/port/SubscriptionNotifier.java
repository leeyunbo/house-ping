package com.yunbok.houseping.core.port;

import com.yunbok.houseping.adapter.dto.SubscriptionInfo;

import java.util.List;

/**
 * 청약 정보 알림을 위한 포트
 */
public interface SubscriptionNotifier {

    /**
     * 신규 청약 정보 리스트 발송
     */
    void sendNewSubscriptions(List<SubscriptionInfo> subscriptions);

    /**
     * 단일 청약 정보 발송
     */
    void sendSubscription(SubscriptionInfo subscription);
}
