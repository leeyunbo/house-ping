package com.yunbok.houseping.support.dto;

import com.yunbok.houseping.core.domain.Subscription;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class AnnouncedSubscriptionView {
    private final Subscription subscription;
    private final BigDecimal topRate;
}
