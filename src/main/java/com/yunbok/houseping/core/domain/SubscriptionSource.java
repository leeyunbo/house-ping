package com.yunbok.houseping.core.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 청약 정보 출처
 */
@Getter
@RequiredArgsConstructor
public enum SubscriptionSource {
    APPLYHOME("ApplyHome"),
    LH("LH");

    private final String value;

    public boolean matches(String source) {
        if (source == null) return false;
        return value.equalsIgnoreCase(source) || source.toUpperCase().contains(name());
    }
}
