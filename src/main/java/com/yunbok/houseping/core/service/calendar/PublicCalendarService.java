package com.yunbok.houseping.core.service.calendar;

import com.querydsl.core.BooleanBuilder;
import com.yunbok.houseping.core.domain.SubscriptionSource;
import com.yunbok.houseping.support.dto.PublicCalendarEventDto;
import com.yunbok.houseping.entity.QSubscriptionEntity;
import com.yunbok.houseping.entity.SubscriptionEntity;
import com.yunbok.houseping.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * 공개 캘린더 서비스
 * 알림 관련 기능 제외, 청약Home + LH 모두 표시
 */
@Service
@RequiredArgsConstructor
public class PublicCalendarService {

    private final SubscriptionRepository subscriptionRepository;

    private static final QSubscriptionEntity subscription = QSubscriptionEntity.subscriptionEntity;

    public List<PublicCalendarEventDto> getCalendarEvents(LocalDate start, LocalDate end) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.or(
                subscription.receiptStartDate.goe(start)
                        .and(subscription.receiptStartDate.loe(end))
        );
        builder.or(
                subscription.receiptEndDate.goe(start)
                        .and(subscription.receiptEndDate.loe(end))
        );
        builder.or(
                subscription.winnerAnnounceDate.isNotNull()
                        .and(subscription.winnerAnnounceDate.goe(start))
                        .and(subscription.winnerAnnounceDate.loe(end))
        );
        builder.or(
                subscription.receiptStartDate.loe(start)
                        .and(subscription.receiptEndDate.goe(end))
        );

        List<SubscriptionEntity> entities = StreamSupport
                .stream(subscriptionRepository.findAll(builder).spliterator(), false)
                .toList();

        // detailUrl 기준 중복 제거
        entities = entities.stream()
                .collect(Collectors.toMap(
                        e -> e.getDetailUrl() != null ? e.getDetailUrl() : "no-url-" + e.getId(),
                        e -> e,
                        (existing, replacement) -> existing
                ))
                .values()
                .stream()
                .toList();

        List<PublicCalendarEventDto> events = new ArrayList<>();

        for (SubscriptionEntity entity : entities) {
            if (entity.getReceiptStartDate() != null) {
                events.add(toCalendarEvent(entity, "receipt"));
            }
            if (entity.getWinnerAnnounceDate() != null) {
                events.add(toCalendarEvent(entity, "winner"));
            }
        }

        return events;
    }

    private PublicCalendarEventDto toCalendarEvent(SubscriptionEntity entity, String eventType) {
        String title;
        LocalDate start;
        LocalDate end;
        String color;
        String textColor = "#ffffff";

        boolean isLH = SubscriptionSource.LH.matches(entity.getSource());
        String sourceTag = isLH ? "[LH]" : "[청약]";

        // 청약Home만 분석 페이지 제공
        boolean hasAnalysis = !isLH;

        boolean expired;
        if ("receipt".equals(eventType)) {
            // 접수 이벤트: 접수 종료일 기준
            expired = entity.getReceiptEndDate() != null
                    && entity.getReceiptEndDate().isBefore(LocalDate.now());
            title = sourceTag + " " + entity.getHouseName();
            start = entity.getReceiptStartDate();
            end = entity.getReceiptEndDate() != null ? entity.getReceiptEndDate().plusDays(1) : start.plusDays(1);
            color = isLH ? "#f97316" : "#3b82f6";
        } else {
            // 발표 이벤트: 발표일 기준
            expired = entity.getWinnerAnnounceDate() != null
                    && entity.getWinnerAnnounceDate().isBefore(LocalDate.now());
            title = sourceTag + " " + entity.getHouseName();
            start = entity.getWinnerAnnounceDate();
            end = start.plusDays(1);
            color = isLH ? "#a855f7" : "#10b981";
        }

        return new PublicCalendarEventDto(
                entity.getId(),
                title,
                start,
                end,
                color,
                textColor,
                new PublicCalendarEventDto.ExtendedProps(
                        entity.getHouseName(),
                        entity.getArea(),
                        entity.getSource(),
                        entity.getHouseType(),
                        entity.getAnnounceDate(),
                        entity.getReceiptStartDate(),
                        entity.getReceiptEndDate(),
                        entity.getWinnerAnnounceDate(),
                        entity.getTotalSupplyCount(),
                        entity.getDetailUrl(),
                        eventType,
                        expired,
                        entity.getAddress(),
                        hasAnalysis
                )
        );
    }
}
