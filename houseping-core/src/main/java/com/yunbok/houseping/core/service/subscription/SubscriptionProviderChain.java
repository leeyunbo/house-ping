package com.yunbok.houseping.core.service.subscription;

import com.yunbok.houseping.core.domain.Subscription;

import java.time.LocalDate;
import java.util.List;

public interface SubscriptionProviderChain {

    List<Subscription> execute(String areaName, LocalDate targetDate);

    List<Subscription> executeAll(String areaName);

    String getSourceName();
}
