package com.yunbok.houseping.domain.port.out;

import com.yunbok.houseping.domain.model.Subscription;

import java.util.List;
import java.util.Optional;

/**
 * 청약 조회 Port (읽기 전용)
 */
public interface SubscriptionQueryPort {

    /**
     * ID로 청약 조회
     */
    Optional<Subscription> findById(Long id);

    /**
     * 지역으로 청약 목록 조회
     */
    List<Subscription> findByAreaContaining(String area);

    /**
     * 소스와 지역으로 청약 목록 조회
     */
    List<Subscription> findBySourceAndAreas(String source, List<String> areas);

    /**
     * 최근 청약 목록 조회 (페이징)
     */
    List<Subscription> findRecentSubscriptions(int limit);
}
