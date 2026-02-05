package com.yunbok.houseping.adapter.in.web.home;

import java.time.LocalDate;

/**
 * 공개 캘린더용 이벤트 DTO
 * 알림 관련 필드 제외
 */
public record PublicCalendarEventDto(
        Long id,
        String title,
        LocalDate start,
        LocalDate end,
        String color,
        String textColor,
        ExtendedProps extendedProps
) {
    public record ExtendedProps(
            String houseName,
            String area,
            String source,
            String houseType,
            LocalDate announceDate,
            LocalDate receiptStartDate,
            LocalDate receiptEndDate,
            LocalDate winnerAnnounceDate,
            Integer totalSupplyCount,
            String detailUrl,
            String eventType,
            boolean expired,
            String address,
            boolean hasAnalysis
    ) {}
}
