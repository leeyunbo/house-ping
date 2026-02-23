package com.yunbok.houseping.core.domain;

import java.util.List;

/**
 * 청약 수집 관련 설정
 */
public record SubscriptionConfig(
    List<String> targetAreas
) {
    public SubscriptionConfig {
        targetAreas = targetAreas != null ? List.copyOf(targetAreas) : List.of("서울", "경기");
    }
}
