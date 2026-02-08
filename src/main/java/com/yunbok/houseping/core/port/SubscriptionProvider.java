package com.yunbok.houseping.core.port;

import com.yunbok.houseping.adapter.dto.SubscriptionInfo;

import java.time.LocalDate;
import java.util.List;

/**
 * 청약 데이터 제공자 통합 인터페이스
 */
public interface SubscriptionProvider {

    /**
     * 특정 지역, 특정 날짜의 청약 데이터 조회
     */
    List<SubscriptionInfo> fetch(String areaName, LocalDate targetDate);

    /**
     * 특정 지역의 전체 청약 데이터 조회 (DB 동기화용)
     */
    default List<SubscriptionInfo> fetchAll(String areaName) {
        return List.of();
    }

    /**
     * 데이터 소스 이름 반환
     */
    default String getSourceName() {
        return getClass().getSimpleName();
    }
}
