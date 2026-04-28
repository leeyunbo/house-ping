package com.yunbok.houseping.core.service.subscription;

import com.yunbok.houseping.core.domain.CompetitionRate;
import com.yunbok.houseping.core.domain.Subscription;
import com.yunbok.houseping.core.domain.SubscriptionStatus;
import com.yunbok.houseping.core.port.CompetitionRatePersistencePort;
import com.yunbok.houseping.core.port.SubscriptionPersistencePort;
import com.yunbok.houseping.support.dto.AnnouncedSubscriptionView;
import com.yunbok.houseping.support.dto.HomePageResult;
import com.yunbok.houseping.support.dto.MonthlyPageResult;
import com.yunbok.houseping.support.dto.SubscriptionCardView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 청약 조회 서비스
 * UseCase를 구현하고 Port를 통해 데이터 접근
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionSearchService {

    private static final List<String> SUPPORTED_AREAS = List.of("서울", "경기");

    private final SubscriptionPersistencePort subscriptionQueryPort;
    private final CompetitionRatePersistencePort competitionRatePort;
    private final PriceBadgeCalculator priceBadgeCalculator;

    public Optional<Subscription> findById(Long id) {
        return subscriptionQueryPort.findById(id);
    }

    public List<Subscription> findActiveAndUpcomingSubscriptions(String area) {
        return findByAreaWithFilter(area).stream()
                .filter(s -> s.getStatus(LocalDate.now()) == SubscriptionStatus.ACTIVE
                        || s.getStatus(LocalDate.now()) == SubscriptionStatus.UPCOMING)
                .toList();
    }

    public List<SubscriptionCardView> getAllActiveAndUpcoming() {
        return findActiveAndUpcomingSubscriptions(null).stream()
                .map(this::toCardView)
                .toList();
    }

    public HomePageResult getHomeData(String area) {
        LocalDate today = LocalDate.now();
        List<Subscription> all = findByAreaWithFilter(area);

        List<SubscriptionCardView> activeCards = all.stream()
                .filter(s -> s.getStatus(today) == SubscriptionStatus.ACTIVE)
                .sorted(Comparator.comparing(Subscription::getReceiptEndDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::toCardView)
                .toList();

        List<SubscriptionCardView> upcomingCards = all.stream()
                .filter(s -> s.getStatus(today) == SubscriptionStatus.UPCOMING)
                .sorted(Comparator.comparing(Subscription::getReceiptStartDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::toCardView)
                .toList();

        return HomePageResult.builder()
                .activeSubscriptions(activeCards)
                .upcomingSubscriptions(upcomingCards)
                .announcedSubscriptions(buildAnnouncedView(all, today))
                .areas(SUPPORTED_AREAS)
                .selectedArea(area)
                .build();
    }

    public List<AnnouncedSubscriptionView> findAnnouncedSubscriptions(String area) {
        return buildAnnouncedView(findByAreaWithFilter(area), LocalDate.now());
    }

    private List<AnnouncedSubscriptionView> buildAnnouncedView(List<Subscription> all, LocalDate today) {
        LocalDate twoWeeksAgo = today.minusWeeks(2);
        Set<String> houseManageNosWithRates = new HashSet<>(competitionRatePort.findDistinctHouseManageNos());

        List<Subscription> announced = all.stream()
                .filter(s -> s.getStatus(today) == SubscriptionStatus.CLOSED)
                .filter(s -> s.getReceiptEndDate() != null && !s.getReceiptEndDate().isBefore(twoWeeksAgo))
                .filter(s -> s.getHouseManageNo() != null && houseManageNosWithRates.contains(s.getHouseManageNo()))
                .toList();

        if (announced.isEmpty()) {
            return List.of();
        }

        Map<String, BigDecimal> topRateByHouseManageNo = computeTopRates(
                announced.stream().map(Subscription::getHouseManageNo).toList()
        );

        return announced.stream()
                .map(s -> AnnouncedSubscriptionView.builder()
                        .subscription(s)
                        .topRate(topRateByHouseManageNo.get(s.getHouseManageNo()))
                        .build())
                .sorted(Comparator.comparing(
                        v -> v.getSubscription().getReceiptStartDate(),
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private Map<String, BigDecimal> computeTopRates(List<String> houseManageNos) {
        return competitionRatePort.findByHouseManageNos(houseManageNos).stream()
                .filter(r -> r.getRank() != null && r.getRank() == 1)
                .filter(r -> "해당지역".equals(r.getResidenceArea()))
                .filter(r -> r.getEffectiveRate() != null)
                .collect(Collectors.toMap(
                        CompetitionRate::getHouseManageNo,
                        CompetitionRate::getEffectiveRate,
                        BigDecimal::max
                ));
    }

    private List<Subscription> filterActiveSubscriptions(List<Subscription> subscriptions) {
        return subscriptions.stream()
                .filter(s -> s.getStatus(LocalDate.now()) == SubscriptionStatus.ACTIVE)
                .toList();
    }

    private List<Subscription> filterUpcomingSubscriptions(List<Subscription> subscriptions) {
        return subscriptions.stream()
                .filter(s -> s.getStatus(LocalDate.now()) == SubscriptionStatus.UPCOMING)
                .toList();
    }

    public MonthlyPageResult getMonthlyData(int year, int month) {
        List<Subscription> subscriptions = findByMonth(year, month);
        return MonthlyPageResult.builder()
                .subscriptions(subscriptions)
                .activeSubscriptions(filterActiveSubscriptions(subscriptions))
                .upcomingSubscriptions(filterUpcomingSubscriptions(subscriptions))
                .closedSubscriptions(subscriptions.stream()
                        .filter(s -> s.getStatus(LocalDate.now()) == SubscriptionStatus.CLOSED)
                        .toList())
                .build();
    }

    public List<Subscription> findSubscriptionsForWeek(LocalDate weekStart, LocalDate weekEnd) {
        return subscriptionQueryPort.findByReceiptPeriodOverlapping(weekStart, weekEnd).stream()
                .filter(s -> s.getArea() != null && SUPPORTED_AREAS.stream()
                        .anyMatch(supported -> s.getArea().contains(supported)))
                .sorted(Comparator.comparing(Subscription::getReceiptStartDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    public List<SubscriptionCardView> getSubscriptionCardsForWeek(LocalDate weekStart, LocalDate weekEnd) {
        return findSubscriptionsForWeek(weekStart, weekEnd).stream()
                .map(this::toCardView)
                .toList();
    }

    public List<Subscription> findByMonth(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate firstDay = ym.atDay(1);
        LocalDate lastDay = ym.atEndOfMonth();
        return subscriptionQueryPort.findByReceiptStartDateBetween(firstDay, lastDay);
    }

    public List<Subscription> findAll() {
        return subscriptionQueryPort.findAll();
    }

    private List<Subscription> findByAreaWithFilter(String area) {
        List<Subscription> subscriptions;
        if (area != null && !area.isBlank()) {
            subscriptions = subscriptionQueryPort.findByAreaContaining(area);
        } else {
            subscriptions = subscriptionQueryPort.findBySupportedAreas(SUPPORTED_AREAS);
        }
        return subscriptions.stream()
                .filter(s -> s.getArea() != null && SUPPORTED_AREAS.stream()
                        .anyMatch(supported -> s.getArea().contains(supported)))
                .toList();
    }

    private SubscriptionCardView toCardView(Subscription s) {
        return SubscriptionCardView.builder()
                .subscription(s)
                .priceBadge(priceBadgeCalculator.computePriceBadge(s))
                .build();
    }
}
