package com.yunbok.houseping.domain.port.outbound;

import com.yunbok.houseping.domain.model.SubscriptionInfo;

import java.time.LocalDate;
import java.util.List;

/**
 * 청약 데이터 수집을 위한 아웃바운드 포트
 */
public interface SubscriptionOuterWorldProvider {

    /**
     * 특정 지역, 특정 날짜의 청약 데이터 조회
     */
    List<SubscriptionInfo> fetch(String areaName, LocalDate targetDate);

    /**
     * 특정 지역의 전체 청약 데이터 조회
     */
    List<SubscriptionInfo> fetchAll(String areaName);
}
