package com.yunbok.houseping.core.service.subscription;

import com.yunbok.houseping.core.domain.Subscription;
import com.yunbok.houseping.core.domain.SubscriptionPrice;
import com.yunbok.houseping.core.domain.SubscriptionStatus;
import com.yunbok.houseping.adapter.persistence.SubscriptionPriceQueryAdapter;
import com.yunbok.houseping.adapter.persistence.SubscriptionQueryAdapter;
import com.yunbok.houseping.support.dto.HomePageResult;
import com.yunbok.houseping.support.dto.MonthlyPageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

/**
 * 청약 조회 서비스
 * UseCase를 구현하고 Port를 통해 데이터 접근
 */
@Service
@RequiredArgsConstructor
public class SubscriptionSearchService {

    private static final List<String> SUPPORTED_AREAS = List.of("서울", "경기");

    private final SubscriptionQueryAdapter subscriptionQueryPort;
    private final SubscriptionPriceQueryAdapter subscriptionPriceQueryPort;

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

    public HomePageResult getHomeData(String area, String status) {
        List<Subscription> subscriptions = findActiveAndUpcomingSubscriptions(area);

        List<Subscription> active;
        List<Subscription> upcoming;

        if ("active".equals(status)) {
            active = filterActiveSubscriptions(subscriptions);
            upcoming = List.of();
        } else if ("upcoming".equals(status)) {
            active = List.of();
            upcoming = filterUpcomingSubscriptions(subscriptions);
        } else {
            active = filterActiveSubscriptions(subscriptions);
            upcoming = filterUpcomingSubscriptions(subscriptions);
        }

        return HomePageResult.builder()
                .activeSubscriptions(active)
                .upcomingSubscriptions(upcoming)
                .areas(SUPPORTED_AREAS)
                .selectedArea(area)
                .selectedStatus(status)
                .build();
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
