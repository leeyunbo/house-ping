package com.yunbok.houseping.domain.port.in;

import com.yunbok.houseping.domain.model.Subscription;
import com.yunbok.houseping.domain.model.SubscriptionPrice;

import java.util.List;
import java.util.Optional;

/**
 * 청약 조회 UseCase
 */
public interface SubscriptionQueryUseCase {

    /**
     * ID로 청약 조회
     */
    Optional<Subscription> findById(Long id);

    /**
     * 서울/경기 청약 목록 조회 (ApplyHome만, 접수중+예정)
     */
    List<Subscription> findActiveAndUpcomingSubscriptions(String area);

    /**
     * 접수중 청약 필터링
     */
    List<Subscription> filterActiveSubscriptions(List<Subscription> subscriptions);

    /**
     * 접수예정 청약 필터링
     */
    List<Subscription> filterUpcomingSubscriptions(List<Subscription> subscriptions);

    /**
     * 분양가 목록 조회
     */
    List<SubscriptionPrice> findPricesByHouseManageNo(String houseManageNo);
}
