package com.yunbok.houseping.domain.port.in;

import com.yunbok.houseping.domain.model.SubscriptionInfo;

import java.time.LocalDate;
import java.util.List;

public interface SubscriptionUseCase {

    List<SubscriptionInfo> collect(LocalDate targetDate, boolean notify);
}
