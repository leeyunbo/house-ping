package com.yunbok.houseping.core.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 청약 상태
 */
@Getter
@RequiredArgsConstructor
public enum SubscriptionStatus {
    ACTIVE("접수중"),
    UPCOMING("접수예정"),
    CLOSED("마감");

    private final String label;
}
