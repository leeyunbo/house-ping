package com.yunbok.houseping.core.port;

/**
 * 분양가 수집 Port (외부 API에서 분양가 조회 후 저장)
 */
public interface SubscriptionPriceCollectPort {

    void fetchAndSavePriceDetails(String houseManageNo, String pblancNo, String houseType);
}
