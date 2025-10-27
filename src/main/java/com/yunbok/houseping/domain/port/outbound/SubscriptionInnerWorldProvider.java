package com.yunbok.houseping.domain.port.outbound;

import com.yunbok.houseping.domain.model.SubscriptionInfo;

import java.time.LocalDate;
import java.util.List;

public interface SubscriptionInnerWorldProvider {

    /**
     * 특정 지역, 특정 날짜의 청약 데이터 조회
     */
    List<SubscriptionInfo> fetch(String areaName, LocalDate targetDate);
}
