package com.yunbok.houseping.domain.service;

import com.yunbok.houseping.domain.model.SubscriptionInfo;

import java.time.LocalDate;
import java.util.List;

public interface SubscriptionProviderOrchestrator {

    List<SubscriptionInfo> orchestrate(String areaName, LocalDate targetDate);
}
