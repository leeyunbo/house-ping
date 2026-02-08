package com.yunbok.houseping.core.service.subscription;

import com.yunbok.houseping.adapter.dto.SubscriptionInfo;

import java.time.LocalDate;
import java.util.List;

public interface SubscriptionProviderChain {

    List<SubscriptionInfo> execute(String areaName, LocalDate targetDate);
}
