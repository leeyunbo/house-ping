package com.yunbok.houseping.adapter.in.web.dto;

import java.time.LocalDate;
import java.util.List;

public record TestCollectResponse(
        LocalDate date,
        int subscriptionsCount,
        List<SubscriptionDto> subscriptions,
        List<String> messagePreview
) {
}
