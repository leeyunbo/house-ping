package com.yunbok.houseping.core.service.subscription;

import com.yunbok.houseping.core.domain.Subscription;
import com.yunbok.houseping.core.domain.SubscriptionPrice;
import com.yunbok.houseping.core.domain.SubscriptionStatus;
import com.yunbok.houseping.adapter.persistence.SubscriptionPriceQueryAdapter;
import com.yunbok.houseping.adapter.persistence.SubscriptionQueryAdapter;
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
import java.math.RoundingMode;
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

    private final SubscriptionQueryAdapter subscriptionQueryPort;
    private final SubscriptionPriceQueryAdapter subscriptionPriceQueryPort;
    private final CompetitionRateRepository competitionRateRepository;
    private final PriceBadgeCalculator priceBadgeCalculator;

    public Optional<Subscription> findById(Long id) {
        return subscriptionQueryPort.findById(id);
    }

    public List<Subscription> findActiveAndUpcomingSubscriptions(String area) {
        List<Subscription> subscriptions;

        if (area != null && !area.isBlank()) {
            subscriptions = subscriptionQueryPort.findByAreaContaining(area);
        } else {
            subscriptions = subscriptionQueryPort.findBySupportedAreas(SUPPORTED_AREAS);
        }

        // 서울/경기만, 접수중+예정만 필터링 (ApplyHome + LH 모두 포함)
        return subscriptions.stream()
                .filter(s -> s.getArea() != null && SUPPORTED_AREAS.stream()
                        .anyMatch(supported -> s.getArea().contains(supported)))
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE
                        || s.getStatus() == SubscriptionStatus.UPCOMING)
                .toList();
    }

    public HomePageResult getHomeData(String area) {
        List<Subscription> activeUpcoming = findActiveAndUpcomingSubscriptions(area);

        List<SubscriptionCardView> activeCards = filterActiveSubscriptions(activeUpcoming).stream()
                .sorted(Comparator.comparing(Subscription::getReceiptEndDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(s -> SubscriptionCardView.builder()
                        .subscription(s)
                        .priceBadge(priceBadgeCalculator.computePriceBadge(s))
                        .build())
                .toList();

        List<SubscriptionCardView> upcomingCards = filterUpcomingSubscriptions(activeUpcoming).stream()
                .sorted(Comparator.comparing(Subscription::getReceiptStartDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(s -> SubscriptionCardView.builder()
                        .subscription(s)
                        .priceBadge(priceBadgeCalculator.computePriceBadge(s))
                        .build())
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

        List<Subscription> allSubscriptions;
        if (area != null && !area.isBlank()) {
            allSubscriptions = subscriptionQueryPort.findByAreaContaining(area);
        } else {
            allSubscriptions = subscriptionQueryPort.findBySupportedAreas(SUPPORTED_AREAS);
        }

        return allSubscriptions.stream()
                .filter(s -> s.getArea() != null && SUPPORTED_AREAS.stream()
                        .anyMatch(supported -> s.getArea().contains(supported)))
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
                .map(this::effectiveRate)
                .filter(rate -> rate != null)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }

    private BigDecimal effectiveRate(CompetitionRateEntity r) {
        if (r.getCompetitionRate() != null) {
            return r.getCompetitionRate();
        }
        if (r.getSupplyCount() != null && r.getSupplyCount() > 0 && r.getRequestCount() != null) {
            return BigDecimal.valueOf(r.getRequestCount())
                    .divide(BigDecimal.valueOf(r.getSupplyCount()), 2, RoundingMode.HALF_UP);
        }
        return null;
    }

    public List<Subscription> filterActiveSubscriptions(List<Subscription> subscriptions) {
        return subscriptions.stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
                .toList();
    }

    public List<Subscription> filterUpcomingSubscriptions(List<Subscription> subscriptions) {
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

    public List<Subscription> findByMonth(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate firstDay = ym.atDay(1);
        LocalDate lastDay = ym.atEndOfMonth();
        return subscriptionQueryPort.findByReceiptStartDateBetween(firstDay, lastDay);
    }

    public List<Subscription> findAll() {
        return subscriptionQueryPort.findAll();
    }

    public List<SubscriptionPrice> findPricesByHouseManageNo(String houseManageNo) {
        if (houseManageNo == null || houseManageNo.isBlank()) {
            return List.of();
        }
        return subscriptionPriceQueryPort.findByHouseManageNo(houseManageNo);
    }

}
