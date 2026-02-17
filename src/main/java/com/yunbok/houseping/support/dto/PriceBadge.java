package com.yunbok.houseping.support.dto;

import lombok.Getter;

@Getter
public enum PriceBadge {

    CHEAP("시세대비↓", "주변 신축 시세 대비 낮은 분양가", "badge-cheap"),
    EXPENSIVE("시세대비↑", "주변 신축 시세 대비 높은 분양가", "badge-expensive"),
    UNKNOWN("비교불가", "시세 비교 데이터가 부족합니다", "badge-unknown");

    private final String label;
    private final String tooltip;
    private final String colorClass;

    PriceBadge(String label, String tooltip, String colorClass) {
        this.label = label;
        this.tooltip = tooltip;
        this.colorClass = colorClass;
    }
}
