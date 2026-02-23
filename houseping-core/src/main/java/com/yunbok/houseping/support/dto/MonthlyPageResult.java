package com.yunbok.houseping.support.dto;

import com.yunbok.houseping.core.domain.Subscription;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MonthlyPageResult {

    private final List<Subscription> subscriptions;
    private final List<Subscription> activeSubscriptions;
    private final List<Subscription> upcomingSubscriptions;
    private final List<Subscription> closedSubscriptions;
}
