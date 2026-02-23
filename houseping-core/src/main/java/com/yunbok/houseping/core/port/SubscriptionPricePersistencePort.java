package com.yunbok.houseping.core.port;

import com.yunbok.houseping.core.domain.SubscriptionPrice;

import java.util.List;

public interface SubscriptionPricePersistencePort {

    List<SubscriptionPrice> findByHouseManageNo(String houseManageNo);
}
