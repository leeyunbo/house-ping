package com.yunbok.houseping.service;

import com.yunbok.houseping.service.dto.AdminSubscriptionDto;
import com.yunbok.houseping.service.dto.AdminSubscriptionSearchCriteria;
import com.yunbok.houseping.core.domain.RealTransaction;
import com.yunbok.houseping.core.domain.SubscriptionPrice;
import com.yunbok.houseping.core.domain.SubscriptionSource;
import com.yunbok.houseping.core.port.SubscriptionPricePersistencePort;
import com.yunbok.houseping.core.service.subscription.SubscriptionAnalysisService;
import com.yunbok.houseping.support.dto.CalendarEventDto;
import com.yunbok.houseping.support.dto.HouseTypeComparison;
import com.yunbok.houseping.support.dto.MarketAnalysis;
import com.yunbok.houseping.support.dto.SubscriptionAnalysisResult;
import com.yunbok.houseping.support.util.AreaNormalizer;

import com.yunbok.houseping.entity.NotificationSubscriptionEntity;
import com.yunbok.houseping.repository.NotificationSubscriptionRepository;
import com.yunbok.houseping.entity.QSubscriptionEntity;
import com.yunbok.houseping.entity.SubscriptionEntity;
import com.yunbok.houseping.repository.SubscriptionRepository;
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
public class AdminSubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final NotificationSubscriptionRepository notificationSubscriptionRepository;
    private final SubscriptionPricePersistencePort subscriptionPricePort;
    private final SubscriptionAnalysisService analysisService;

    private static final QSubscriptionEntity subscription = QSubscriptionEntity.subscriptionEntity;

    @Transactional(readOnly = true)
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
                entity.getZipCode(),
                entity.getHouseManageNo()
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

        entities = entities.stream()
                .collect(Collectors.toMap(
                        e -> e.getDetailUrl() != null ? e.getDetailUrl() : "no-url-" + e.getId(),
                        e -> e,
                        (existing, replacement) -> existing
                ))
                .values()
                .stream()
                .toList();

        List<Long> subscriptionIds = entities.stream().map(SubscriptionEntity::getId).toList();
        Set<Long> notificationEnabledIds = notificationSubscriptionRepository
                .findBySubscriptionIdInAndEnabledTrue(subscriptionIds)
                .stream()
                .map(NotificationSubscriptionEntity::getSubscriptionId)
                .collect(Collectors.toSet());

        List<CalendarEventDto> events = new ArrayList<>();

        for (SubscriptionEntity entity : entities) {
            boolean notificationEnabled = notificationEnabledIds.contains(entity.getId());
            if (entity.getReceiptStartDate() != null) {
                events.add(toCalendarEvent(entity, "receipt", notificationEnabled));
            }
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

        boolean isLH = SubscriptionSource.LH.matches(entity.getSource());
        String sourceTag = isLH ? "[LH]" : "[청약]";

        boolean expired;
        if ("receipt".equals(eventType)) {
            expired = entity.getReceiptEndDate() != null
                    && entity.getReceiptEndDate().isBefore(LocalDate.now());
            title = sourceTag + " " + entity.getHouseName();
            start = entity.getReceiptStartDate();
            end = entity.getReceiptEndDate() != null ? entity.getReceiptEndDate().plusDays(1) : start.plusDays(1);
            color = isLH ? "#f97316" : "#3b82f6";
        } else {
            expired = entity.getWinnerAnnounceDate() != null
                    && entity.getWinnerAnnounceDate().isBefore(LocalDate.now());
            title = sourceTag + " " + entity.getHouseName();
            start = entity.getWinnerAnnounceDate();
            end = start.plusDays(1);
            color = isLH ? "#a855f7" : "#10b981";
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

    @Transactional
    public boolean toggleNotification(Long subscriptionId) {
        Optional<NotificationSubscriptionEntity> existing =
                notificationSubscriptionRepository.findBySubscriptionId(subscriptionId);

        if (existing.isPresent()) {
            NotificationSubscriptionEntity entity = existing.get();
            entity.toggleEnabled();
            return entity.isEnabled();
        } else {
            NotificationSubscriptionEntity newEntity = NotificationSubscriptionEntity.builder()
                    .subscriptionId(subscriptionId)
                    .enabled(true)
                    .build();
            notificationSubscriptionRepository.save(newEntity);
            return true;
        }
    }

    @Transactional
    public void removeNotification(Long subscriptionId) {
        notificationSubscriptionRepository.deleteBySubscriptionId(subscriptionId);
    }

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

    public List<PriceDto> getPrices(Long subscriptionId) {
        return findById(subscriptionId)
                .map(sub -> {
                    if (sub.houseManageNo() == null || sub.houseManageNo().isEmpty()) {
                        return List.<PriceDto>of();
                    }
                    return subscriptionPricePort.findByHouseManageNo(sub.houseManageNo())
                            .stream()
                            .map(PriceDto::from)
                            .toList();
                })
                .orElse(List.of());
    }

    public MarketAnalysisDto getMarketAnalysis(Long subscriptionId) {
        try {
            SubscriptionAnalysisResult analysis = analysisService.analyze(subscriptionId);
            MarketAnalysis market = analysis.getMarketAnalysis();

            List<HouseTypeComparisonDto> comparisons = analysis.getHouseTypeComparisons().stream()
                    .map(HouseTypeComparisonDto::from)
                    .toList();

            if (market == null) {
                return new MarketAnalysisDto(
                        null, null, null, null, 0,
                        analysis.getDongName(),
                        List.of(),
                        comparisons
                );
            }

            List<TransactionDto> transactions = analysis.getRecentTransactions().stream()
                    .limit(5)
                    .map(TransactionDto::from)
                    .toList();

            return new MarketAnalysisDto(
                    market.getAverageAmountFormatted(),
                    formatPricePerPyeong(market.getAveragePricePerPyeong()),
                    market.getMaxAmountFormatted(),
                    market.getMinAmountFormatted(),
                    market.getTransactionCount(),
                    analysis.getDongName(),
                    transactions,
                    comparisons
            );
        } catch (Exception e) {
            return new MarketAnalysisDto(null, null, null, null, 0, null, List.of(), List.of());
        }
    }

    private String formatPricePerPyeong(long price) {
        if (price == 0) return "-";
        return String.format("%,d만/평", price);
    }

    public record PriceDto(
            String houseType,
            Double supplyArea,
            Integer supplyCount,
            Integer specialSupplyCount,
            Long topAmount,
            Long pricePerPyeong
    ) {
        public static PriceDto from(SubscriptionPrice price) {
            return new PriceDto(
                    price.getHouseType(),
                    price.getSupplyArea() != null ? price.getSupplyArea().doubleValue() : null,
                    price.getSupplyCount(),
                    price.getSpecialSupplyCount(),
                    price.getTopAmount(),
                    price.getPricePerPyeong()
            );
        }
    }

    public record MarketAnalysisDto(
            String averageAmount,
            String pricePerPyeong,
            String maxAmount,
            String minAmount,
            int transactionCount,
            String dongName,
            List<TransactionDto> recentTransactions,
            List<HouseTypeComparisonDto> houseTypeComparisons
    ) {}

    public record HouseTypeComparisonDto(
            String houseType,
            String supplyArea,
            String supplyPrice,
            String marketPrice,
            String estimatedProfit,
            String transactionInfo,
            int transactionCount,
            boolean hasProfit
    ) {
        public static HouseTypeComparisonDto from(HouseTypeComparison c) {
            String areaStr = c.getSupplyArea() != null
                    ? c.getSupplyArea().setScale(0, java.math.RoundingMode.HALF_UP) + "㎡"
                    : "-";

            int txCount = c.getSimilarTransactions() != null ? c.getSimilarTransactions().size() : 0;

            return new HouseTypeComparisonDto(
                    c.getHouseType(),
                    areaStr,
                    c.getSupplyPriceFormatted(),
                    c.getMarketPriceFormatted(),
                    c.getEstimatedProfitFormatted(),
                    c.getTransactionInfo(),
                    txCount,
                    c.hasProfit()
            );
        }
    }

    public record TransactionDto(
            String aptName,
            String area,
            Integer floor,
            String amount,
            String dealDate
    ) {
        public static TransactionDto from(RealTransaction tx) {
            String amountStr = tx.getDealAmount() >= 10000
                    ? String.format("%.1f억", tx.getDealAmount() / 10000.0)
                    : String.format("%,d만", tx.getDealAmount());

            String dateStr = tx.getDealDate() != null
                    ? tx.getDealDate().toString()
                    : "-";

            String areaStr = tx.getExclusiveArea() != null
                    ? tx.getExclusiveArea().setScale(0, java.math.RoundingMode.HALF_UP) + "㎡"
                    : "-";

            return new TransactionDto(
                    tx.getAptName(),
                    areaStr,
                    tx.getFloor(),
                    amountStr,
                    dateStr
            );
        }
    }
}
