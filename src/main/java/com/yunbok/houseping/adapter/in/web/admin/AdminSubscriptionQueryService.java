package com.yunbok.houseping.adapter.in.web.admin;

import com.yunbok.houseping.domain.model.AreaNormalizer;

import com.yunbok.houseping.infrastructure.persistence.NotificationSubscriptionEntity;
import com.yunbok.houseping.infrastructure.persistence.NotificationSubscriptionRepository;
import com.yunbok.houseping.infrastructure.persistence.QSubscriptionEntity;
import com.yunbok.houseping.infrastructure.persistence.SubscriptionEntity;
import com.yunbok.houseping.infrastructure.persistence.SubscriptionRepository;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class AdminSubscriptionQueryService {

    private final SubscriptionRepository subscriptionRepository;
    private final NotificationSubscriptionRepository notificationSubscriptionRepository;

    private static final QSubscriptionEntity subscription = QSubscriptionEntity.subscriptionEntity;

    public Page<AdminSubscriptionDto> search(AdminSubscriptionSearchCriteria criteria) {
        BooleanBuilder builder = new BooleanBuilder();

        if (StringUtils.hasText(criteria.keyword())) {
            builder.and(subscription.houseName.containsIgnoreCase(criteria.keyword().trim()));
        }
        if (StringUtils.hasText(criteria.area())) {
            builder.and(subscription.area.in(AreaNormalizer.expand(criteria.area().trim())));
        }
        if (StringUtils.hasText(criteria.houseType())) {
            builder.and(subscription.houseType.equalsIgnoreCase(criteria.houseType().trim()));
        }
        if (StringUtils.hasText(criteria.source())) {
            builder.and(subscription.source.equalsIgnoreCase(criteria.source().trim()));
        }
        if (criteria.startDate() != null && criteria.endDate() != null) {
            builder.and(subscription.receiptStartDate.between(criteria.startDate(), criteria.endDate()));
        } else if (criteria.startDate() != null) {
            builder.and(subscription.receiptStartDate.goe(criteria.startDate()));
        } else if (criteria.endDate() != null) {
            builder.and(subscription.receiptStartDate.loe(criteria.endDate()));
        }

        Sort sort = Sort.by(
                Sort.Order.desc("receiptStartDate"),
                Sort.Order.desc("createdAt")
        );
        PageRequest pageRequest = PageRequest.of(criteria.page(), criteria.size(), sort);

        Page<SubscriptionEntity> page = subscriptionRepository.findAll(builder, pageRequest);

        // 알림 설정된 청약 ID 조회
        List<Long> subscriptionIds = page.getContent().stream().map(SubscriptionEntity::getId).toList();
        Set<Long> notificationEnabledIds = notificationSubscriptionRepository
                .findBySubscriptionIdInAndEnabledTrue(subscriptionIds)
                .stream()
                .map(NotificationSubscriptionEntity::getSubscriptionId)
                .collect(Collectors.toSet());

        return page.map(entity -> toDto(entity, notificationEnabledIds.contains(entity.getId())));
    }

    private AdminSubscriptionDto toDto(SubscriptionEntity entity) {
        return toDto(entity, false);
    }

    private AdminSubscriptionDto toDto(SubscriptionEntity entity, boolean notificationEnabled) {
        boolean expired = entity.getReceiptEndDate() != null
                && entity.getReceiptEndDate().isBefore(LocalDate.now());
        return new AdminSubscriptionDto(
                entity.getId(),
                entity.getSource(),
                entity.getHouseName(),
                entity.getHouseType(),
                entity.getArea(),
                entity.getAnnounceDate(),
                entity.getReceiptStartDate(),
                entity.getReceiptEndDate(),
                entity.getWinnerAnnounceDate(),
                entity.getDetailUrl(),
                entity.getHomepageUrl(),
                entity.getContact(),
                entity.getTotalSupplyCount(),
                entity.getCollectedAt(),
                entity.getCreatedAt(),
                notificationEnabled,
                expired,
                entity.getAddress(),
                entity.getZipCode()
        );
    }

    public List<String> availableAreas() {
        return subscriptionRepository.findDistinctAreas().stream()
                .map(AreaNormalizer::normalize)
                .distinct()
                .sorted()
                .toList();
    }

    public List<String> availableHouseTypes() {
        return subscriptionRepository.findDistinctHouseTypes();
    }

    public List<String> availableSources() {
        return subscriptionRepository.findDistinctSources();
    }

    public List<CalendarEventDto> getCalendarEvents(LocalDate start, LocalDate end) {
        // 캘린더 범위 내에 있는 청약 조회 (접수 기간 또는 당첨 발표일이 범위 내에 있는 경우)
        BooleanBuilder builder = new BooleanBuilder();
        builder.or(
                // 접수 시작일이 범위 내
                subscription.receiptStartDate.goe(start)
                        .and(subscription.receiptStartDate.loe(end))
        );
        builder.or(
                // 접수 종료일이 범위 내
                subscription.receiptEndDate.goe(start)
                        .and(subscription.receiptEndDate.loe(end))
        );
        builder.or(
                // 당첨 발표일이 범위 내
                subscription.winnerAnnounceDate.isNotNull()
                        .and(subscription.winnerAnnounceDate.goe(start))
                        .and(subscription.winnerAnnounceDate.loe(end))
        );
        builder.or(
                // 접수 기간이 범위를 포함
                subscription.receiptStartDate.loe(start)
                        .and(subscription.receiptEndDate.goe(end))
        );

        List<SubscriptionEntity> entities = StreamSupport
                .stream(subscriptionRepository.findAll(builder).spliterator(), false)
                .toList();

        // detailUrl 기준 중복 제거 (같은 청약이 여러 source로 저장된 경우)
        entities = entities.stream()
                .collect(Collectors.toMap(
                        e -> e.getDetailUrl() != null ? e.getDetailUrl() : "no-url-" + e.getId(),
                        e -> e,
                        (existing, replacement) -> existing  // 첫 번째 항목 유지
                ))
                .values()
                .stream()
                .toList();

        // 알림 설정된 청약 ID 조회
        List<Long> subscriptionIds = entities.stream().map(SubscriptionEntity::getId).toList();
        Set<Long> notificationEnabledIds = notificationSubscriptionRepository
                .findBySubscriptionIdInAndEnabledTrue(subscriptionIds)
                .stream()
                .map(NotificationSubscriptionEntity::getSubscriptionId)
                .collect(Collectors.toSet());

        List<CalendarEventDto> events = new ArrayList<>();

        for (SubscriptionEntity entity : entities) {
            boolean notificationEnabled = notificationEnabledIds.contains(entity.getId());
            // 접수 기간 이벤트
            if (entity.getReceiptStartDate() != null) {
                events.add(toCalendarEvent(entity, "receipt", notificationEnabled));
            }
            // 당첨 발표일 이벤트
            if (entity.getWinnerAnnounceDate() != null) {
                events.add(toCalendarEvent(entity, "winner", notificationEnabled));
            }
        }

        return events;
    }

    private CalendarEventDto toCalendarEvent(SubscriptionEntity entity, String eventType, boolean notificationEnabled) {
        String title;
        LocalDate start;
        LocalDate end;
        String color;
        String textColor = "#ffffff";

        boolean isLH = entity.getSource() != null && entity.getSource().toUpperCase().contains("LH");
        String sourceTag = isLH ? "[LH]" : "[청약]";

        // 접수 종료일이 오늘 이전이면 만료
        boolean expired = entity.getReceiptEndDate() != null
                && entity.getReceiptEndDate().isBefore(LocalDate.now());

        if ("receipt".equals(eventType)) {
            title = sourceTag + " " + entity.getHouseName();
            start = entity.getReceiptStartDate();
            // FullCalendar는 end를 exclusive로 처리하므로 +1일
            end = entity.getReceiptEndDate() != null ? entity.getReceiptEndDate().plusDays(1) : start.plusDays(1);
            color = isLH ? "#f97316" : "#3b82f6"; // LH: 오렌지, 청약Home: 파란색
        } else {
            title = sourceTag + " " + entity.getHouseName();
            start = entity.getWinnerAnnounceDate();
            end = start.plusDays(1);
            color = isLH ? "#a855f7" : "#10b981"; // LH: 보라색, 청약Home: 초록색
        }

        return new CalendarEventDto(
                entity.getId(),
                title,
                start,
                end,
                color,
                textColor,
                new CalendarEventDto.ExtendedProps(
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
                        notificationEnabled,
                        expired,
                        entity.getAddress(),
                        entity.getZipCode()
                )
        );
    }

    public Optional<AdminSubscriptionDto> findById(Long id) {
        return subscriptionRepository.findById(id).map(this::toDto);
    }

    /**
     * 알림 구독 토글
     */
    @Transactional
    public boolean toggleNotification(Long subscriptionId) {
        Optional<NotificationSubscriptionEntity> existing =
                notificationSubscriptionRepository.findBySubscriptionId(subscriptionId);

        if (existing.isPresent()) {
            NotificationSubscriptionEntity entity = existing.get();
            entity.toggleEnabled();
            return entity.isEnabled();
        } else {
            // 새로 생성
            NotificationSubscriptionEntity newEntity = NotificationSubscriptionEntity.builder()
                    .subscriptionId(subscriptionId)
                    .enabled(true)
                    .build();
            notificationSubscriptionRepository.save(newEntity);
            return true;
        }
    }

    /**
     * 알림 구독 해제
     */
    @Transactional
    public void removeNotification(Long subscriptionId) {
        notificationSubscriptionRepository.deleteBySubscriptionId(subscriptionId);
    }

    /**
     * 일괄 알림 설정
     */
    @Transactional
    public int enableNotifications(List<Long> subscriptionIds) {
        int count = 0;
        for (Long subscriptionId : subscriptionIds) {
            Optional<NotificationSubscriptionEntity> existing =
                    notificationSubscriptionRepository.findBySubscriptionId(subscriptionId);

            if (existing.isPresent()) {
                NotificationSubscriptionEntity entity = existing.get();
                if (!entity.isEnabled()) {
                    entity.toggleEnabled();
                    count++;
                }
            } else {
                NotificationSubscriptionEntity newEntity = NotificationSubscriptionEntity.builder()
                        .subscriptionId(subscriptionId)
                        .enabled(true)
                        .build();
                notificationSubscriptionRepository.save(newEntity);
                count++;
            }
        }
        return count;
    }
}
