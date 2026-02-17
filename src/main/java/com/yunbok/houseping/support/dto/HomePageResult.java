package com.yunbok.houseping.support.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class HomePageResult {

    private final List<SubscriptionCardView> activeSubscriptions;
    private final List<SubscriptionCardView> upcomingSubscriptions;
    private final List<AnnouncedSubscriptionView> announcedSubscriptions;
    private final List<String> areas;
    private final String selectedArea;
}
