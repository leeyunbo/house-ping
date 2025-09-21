package com.yunbok.houseping.domain.port;

import com.yunbok.houseping.domain.model.SubscriptionInfo;

import java.util.List;

/**
 * 청약 정보 수집 유스케이스 (인바운드 포트)
 */
public interface CollectSubscriptionUseCase {
    
    /**
     * 오늘의 신규 청약 정보를 수집하여 알림 발송
     * @return 수집된 신규 청약 정보 목록
     */
    List<SubscriptionInfo> collectAndNotifyTodaySubscriptions();
}
