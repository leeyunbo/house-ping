package com.yunbok.houseping.domain.port.out;

import com.yunbok.houseping.domain.model.SubscriptionPrice;

import java.util.List;

/**
 * 분양가 조회 Port
 */
public interface SubscriptionPriceQueryPort {

    /**
     * 주택관리번호로 분양가 목록 조회
     */
    List<SubscriptionPrice> findByHouseManageNo(String houseManageNo);
}
