package com.yunbok.houseping.support.dto;

import java.time.LocalDate;

/**
 * FullCalendar.js 이벤트 형식에 맞춘 DTO
 */
public record CalendarEventDto(
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
            boolean notificationEnabled,
            boolean expired,
            String address,
            String zipCode
    ) {}
}
