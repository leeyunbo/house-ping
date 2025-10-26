package com.yunbok.houseping.domain.port.outbound;

import com.yunbok.houseping.domain.model.SubscriptionInfo;

import java.time.LocalDate;
import java.util.List;

/**
 * 청약 데이터 수집을 위한 아웃바운드 포트
 */
public interface SubscriptionDataProvider {

    List<SubscriptionInfo> fetch(String areaName, LocalDate targetDate);
}
