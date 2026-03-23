package com.yunbok.houseping.core.service.calendar;

import com.yunbok.houseping.core.domain.Subscription;
import com.yunbok.houseping.core.domain.SubscriptionSource;
import com.yunbok.houseping.core.port.SubscriptionPersistencePort;
import com.yunbok.houseping.support.dto.PublicCalendarEventDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 공개 캘린더 서비스
 * 알림 관련 기능 제외, 청약Home + LH 모두 표시
 */
@Service
@RequiredArgsConstructor
public class PublicCalendarService {

    private final SubscriptionPersistencePort subscriptionPort;

    public List<PublicCalendarEventDto> getCalendarEvents(LocalDate start, LocalDate end) {
        List<Subscription> subscriptions = subscriptionPort.findByCalendarPeriod(start, end);

        // detailUrl 기준 중복 제거
        subscriptions = subscriptions.stream()
                .collect(Collectors.toMap(
                        s -> s.getDetailUrl() != null ? s.getDetailUrl() : "no-url-" + s.getId(),
                        s -> s,
                        (existing, replacement) -> existing
                ))
                .values()
                .stream()
                .toList();

        List<PublicCalendarEventDto> events = new ArrayList<>();

        for (Subscription s : subscriptions) {
            if (s.getReceiptStartDate() != null) {
                events.add(toCalendarEvent(s, "receipt"));
            }
            if (s.getWinnerAnnounceDate() != null) {
                events.add(toCalendarEvent(s, "winner"));
            }
        }

        return events;
    }

    private PublicCalendarEventDto toCalendarEvent(Subscription s, String eventType) {
        String title;
        LocalDate start;
        LocalDate end;
        String color;
        String textColor = "#ffffff";

        boolean isLH = SubscriptionSource.LH.matches(s.getSource());
        String sourceTag = isLH ? "[LH]" : "[청약]";
        boolean hasAnalysis = !isLH;

        boolean expired;
        if ("receipt".equals(eventType)) {
            expired = s.getReceiptEndDate() != null && s.getReceiptEndDate().isBefore(LocalDate.now());
            title = sourceTag + " " + s.getHouseName();
            start = s.getReceiptStartDate();
            end = s.getReceiptEndDate() != null ? s.getReceiptEndDate().plusDays(1) : start.plusDays(1);
            color = isLH ? "#f97316" : "#3b82f6";
        } else {
            expired = s.getWinnerAnnounceDate() != null && s.getWinnerAnnounceDate().isBefore(LocalDate.now());
            title = sourceTag + " " + s.getHouseName();
            start = s.getWinnerAnnounceDate();
            end = start.plusDays(1);
            color = isLH ? "#a855f7" : "#10b981";
        }

        return new PublicCalendarEventDto(
                s.getId(), title, start, end, color, textColor,
                new PublicCalendarEventDto.ExtendedProps(
                        s.getHouseName(), s.getArea(), s.getSource(), s.getHouseType(),
                        s.getAnnounceDate(), s.getReceiptStartDate(), s.getReceiptEndDate(),
                        s.getWinnerAnnounceDate(), s.getTotalSupplyCount(), s.getDetailUrl(),
                        eventType, expired, s.getAddress(), hasAnalysis
                )
        );
    }
}
