package com.yunbok.houseping.core.service.subscription;

import com.yunbok.houseping.core.domain.Subscription;
import com.yunbok.houseping.core.domain.SubscriptionStatus;
import com.yunbok.houseping.core.port.SubscriptionPersistencePort;
import com.yunbok.houseping.entity.CompetitionRateEntity;
import com.yunbok.houseping.repository.CompetitionRateRepository;
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
import java.util.Optional;
import java.util.Set;

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
    private final CompetitionRateRepository competitionRateRepository;
    private final PriceBadgeCalculator priceBadgeCalculator;

    public Optional<Subscription> findById(Long id) {
        return subscriptionQueryPort.findById(id);
    }

    public List<Subscription> findActiveAndUpcomingSubscriptions(String area) {
        return findByAreaWithFilter(area).stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE
                        || s.getStatus() == SubscriptionStatus.UPCOMING)
                .toList();
    }

    public List<SubscriptionCardView> getAllActiveAndUpcoming() {
        return findActiveAndUpcomingSubscriptions(null).stream()
                .map(this::toCardView)
                .toList();
    }

    public HomePageResult getHomeData(String area) {
        List<Subscription> activeUpcoming = findActiveAndUpcomingSubscriptions(area);

        List<SubscriptionCardView> activeCards = filterActiveSubscriptions(activeUpcoming).stream()
                .sorted(Comparator.comparing(Subscription::getReceiptEndDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::toCardView)
                .toList();

        List<SubscriptionCardView> upcomingCards = filterUpcomingSubscriptions(activeUpcoming).stream()
                .sorted(Comparator.comparing(Subscription::getReceiptStartDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::toCardView)
                .toList();

        return HomePageResult.builder()
                .activeSubscriptions(activeCards)
                .upcomingSubscriptions(upcomingCards)
                .announcedSubscriptions(findAnnouncedSubscriptions(area))
                .areas(SUPPORTED_AREAS)
                .selectedArea(area)
                .build();
    }

    public List<AnnouncedSubscriptionView> findAnnouncedSubscriptions(String area) {
        Set<String> houseManageNosWithRates = new HashSet<>(competitionRateRepository.findDistinctHouseManageNos());
        LocalDate twoWeeksAgo = LocalDate.now().minusWeeks(2);

        return findByAreaWithFilter(area).stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.CLOSED)
                .filter(s -> s.getReceiptEndDate() != null && !s.getReceiptEndDate().isBefore(twoWeeksAgo))
                .filter(s -> s.getHouseManageNo() != null && houseManageNosWithRates.contains(s.getHouseManageNo()))
                .map(s -> {
                    BigDecimal topRate = computeTopRate(s.getHouseManageNo());
                    return AnnouncedSubscriptionView.builder()
                            .subscription(s)
                            .topRate(topRate)
                            .build();
                })
                .sorted(Comparator.comparing(
                        v -> v.getSubscription().getReceiptStartDate(),
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private BigDecimal computeTopRate(String houseManageNo) {
        List<CompetitionRateEntity> rates = competitionRateRepository.findByHouseManageNo(houseManageNo);
        return rates.stream()
                .filter(r -> r.getRank() != null && r.getRank() == 1)
                .filter(r -> "해당지역".equals(r.getResidenceArea()))
                .map(CompetitionRateEntity::getEffectiveRate)
                .filter(rate -> rate != null)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }

    private List<Subscription> filterActiveSubscriptions(List<Subscription> subscriptions) {
        return subscriptions.stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
                .toList();
    }

    private List<Subscription> filterUpcomingSubscriptions(List<Subscription> subscriptions) {
        return subscriptions.stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.UPCOMING)
                .toList();
    }

    public MonthlyPageResult getMonthlyData(int year, int month) {
        List<Subscription> subscriptions = findByMonth(year, month);
        return MonthlyPageResult.builder()
                .subscriptions(subscriptions)
                .activeSubscriptions(filterActiveSubscriptions(subscriptions))
                .upcomingSubscriptions(filterUpcomingSubscriptions(subscriptions))
                .closedSubscriptions(subscriptions.stream()
                        .filter(s -> s.getStatus() == SubscriptionStatus.CLOSED)
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
