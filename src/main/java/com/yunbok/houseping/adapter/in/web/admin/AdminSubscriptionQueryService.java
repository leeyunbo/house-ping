package com.yunbok.houseping.adapter.in.web.admin;

import com.yunbok.houseping.infrastructure.persistence.NotificationSubscriptionEntity;
import com.yunbok.houseping.infrastructure.persistence.NotificationSubscriptionRepository;
import com.yunbok.houseping.infrastructure.persistence.SubscriptionEntity;
import com.yunbok.houseping.infrastructure.persistence.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminSubscriptionQueryService {

    private final SubscriptionRepository subscriptionRepository;
    private final NotificationSubscriptionRepository notificationSubscriptionRepository;

    public Page<AdminSubscriptionDto> search(AdminSubscriptionSearchCriteria criteria) {
        Specification<SubscriptionEntity> spec = alwaysTrue();

        if (StringUtils.hasText(criteria.keyword())) {
            spec = spec.and(houseNameLike(criteria.keyword()));
        }
        if (StringUtils.hasText(criteria.area())) {
            spec = spec.and(equalsIgnoreCase("area", criteria.area()));
        }
        if (StringUtils.hasText(criteria.source())) {
            spec = spec.and(equalsIgnoreCase("source", criteria.source()));
        }
        if (criteria.startDate() != null || criteria.endDate() != null) {
            spec = spec.and(receiptBetween(criteria.startDate(), criteria.endDate()));
        }

        Sort sort = Sort.by(
                Sort.Order.desc("receiptStartDate"),
                Sort.Order.desc("createdAt")
        );
        PageRequest pageRequest = PageRequest.of(criteria.page(), criteria.size(), sort);

        return subscriptionRepository.findAll(spec, pageRequest).map(this::toDto);
    }

    private AdminSubscriptionDto toDto(SubscriptionEntity entity) {
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
                entity.getCreatedAt()
        );
    }

    private Specification<SubscriptionEntity> alwaysTrue() {
        return (root, query, cb) -> cb.conjunction();
    }

    public List<String> availableAreas() {
        return subscriptionRepository.findDistinctAreas();
    }

    public List<String> availableSources() {
        return subscriptionRepository.findDistinctSources();
    }

    private Specification<SubscriptionEntity> houseNameLike(String keyword) {
        String likeKeyword = "%" + keyword.trim().toLowerCase() + "%";
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("houseName")), likeKeyword);
    }

    private Specification<SubscriptionEntity> equalsIgnoreCase(String field, String value) {
        return (root, query, cb) ->
                cb.equal(cb.lower(root.get(field)), value.trim().toLowerCase());
    }

    private Specification<SubscriptionEntity> receiptBetween(LocalDate start, LocalDate end) {
        return (root, query, cb) -> {
            if (start != null && end != null) {
                return cb.between(root.get("receiptStartDate"), start, end);
            } else if (start != null) {
                return cb.greaterThanOrEqualTo(root.get("receiptStartDate"), start);
            } else if (end != null) {
                return cb.lessThanOrEqualTo(root.get("receiptStartDate"), end);
            }
            return cb.conjunction();
        };
    }

    public List<CalendarEventDto> getCalendarEvents(LocalDate start, LocalDate end) {
        // 캘린더 범위 내에 있는 청약 조회 (접수 기간 또는 당첨 발표일이 범위 내에 있는 경우)
        Specification<SubscriptionEntity> spec = (root, query, cb) -> cb.or(
                // 접수 시작일이 범위 내
                cb.and(
                        cb.greaterThanOrEqualTo(root.get("receiptStartDate"), start),
                        cb.lessThanOrEqualTo(root.get("receiptStartDate"), end)
                ),
                // 접수 종료일이 범위 내
                cb.and(
                        cb.greaterThanOrEqualTo(root.get("receiptEndDate"), start),
                        cb.lessThanOrEqualTo(root.get("receiptEndDate"), end)
                ),
                // 당첨 발표일이 범위 내
                cb.and(
                        cb.isNotNull(root.get("winnerAnnounceDate")),
                        cb.greaterThanOrEqualTo(root.get("winnerAnnounceDate"), start),
                        cb.lessThanOrEqualTo(root.get("winnerAnnounceDate"), end)
                ),
                // 접수 기간이 범위를 포함
                cb.and(
                        cb.lessThanOrEqualTo(root.get("receiptStartDate"), start),
                        cb.greaterThanOrEqualTo(root.get("receiptEndDate"), end)
                )
        );

        List<SubscriptionEntity> entities = subscriptionRepository.findAll(spec);

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
                        notificationEnabled
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
}
