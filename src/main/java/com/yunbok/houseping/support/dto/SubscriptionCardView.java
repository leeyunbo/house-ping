package com.yunbok.houseping.support.dto;

import com.yunbok.houseping.core.domain.Subscription;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubscriptionCardView {
    private final Subscription subscription;
    private final PriceBadge priceBadge;
}
