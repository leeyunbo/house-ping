package com.yunbok.houseping.domain.service;

import com.yunbok.houseping.domain.model.Subscription;
import com.yunbok.houseping.domain.model.SubscriptionPrice;
import com.yunbok.houseping.domain.model.SubscriptionStatus;
import com.yunbok.houseping.domain.port.in.SubscriptionQueryUseCase;
import com.yunbok.houseping.domain.port.out.SubscriptionPriceQueryPort;
import com.yunbok.houseping.domain.port.out.SubscriptionQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 청약 조회 서비스
 * UseCase를 구현하고 Port를 통해 데이터 접근
 */
@Service
@RequiredArgsConstructor
public class SubscriptionQueryService implements SubscriptionQueryUseCase {

    private static final String APPLY_HOME_SOURCE = "ApplyHome";
    private static final List<String> SUPPORTED_AREAS = List.of("서울", "경기");

    private final SubscriptionQueryPort subscriptionQueryPort;
    private final SubscriptionPriceQueryPort subscriptionPriceQueryPort;

    @Override
    public Optional<Subscription> findById(Long id) {
        return subscriptionQueryPort.findById(id);
    }

    @Override
    public List<Subscription> findActiveAndUpcomingSubscriptions(String area) {
        List<Subscription> subscriptions;

        if (area != null && !area.isBlank()) {
            subscriptions = subscriptionQueryPort.findByAreaContaining(area);
        } else {
            subscriptions = subscriptionQueryPort.findBySourceAndAreas(APPLY_HOME_SOURCE, SUPPORTED_AREAS);
        }

        // ApplyHome만, 서울/경기만, 접수중+예정만 필터링
        return subscriptions.stream()
                .filter(s -> APPLY_HOME_SOURCE.equals(s.getSource()))
                .filter(s -> s.getArea() != null && SUPPORTED_AREAS.stream()
                        .anyMatch(supported -> s.getArea().contains(supported)))
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE
                        || s.getStatus() == SubscriptionStatus.UPCOMING)
                .toList();
    }

    @Override
    public List<Subscription> filterActiveSubscriptions(List<Subscription> subscriptions) {
        return subscriptions.stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
                .toList();
    }

    @Override
    public List<Subscription> filterUpcomingSubscriptions(List<Subscription> subscriptions) {
        return subscriptions.stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.UPCOMING)
                .toList();
    }

    @Override
    public List<SubscriptionPrice> findPricesByHouseManageNo(String houseManageNo) {
        if (houseManageNo == null || houseManageNo.isBlank()) {
            return List.of();
        }
        return subscriptionPriceQueryPort.findByHouseManageNo(houseManageNo);
    }
}
