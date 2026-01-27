package com.yunbok.houseping.domain.service;

import com.yunbok.houseping.domain.model.SubscriptionInfo;

import java.time.LocalDate;
import java.util.List;

public interface SubscriptionProviderChain {

    List<SubscriptionInfo> execute(String areaName, LocalDate targetDate);
}
