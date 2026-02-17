package com.yunbok.houseping.core.service.subscription;

import com.yunbok.houseping.adapter.persistence.RealTransactionQueryAdapter;
import com.yunbok.houseping.core.domain.RealTransaction;
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
import com.yunbok.houseping.support.dto.PriceBadge;
import com.yunbok.houseping.support.dto.SubscriptionCardView;
import com.yunbok.houseping.support.util.AddressHelper;
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
    private static final BigDecimal AREA_TOLERANCE = new BigDecimal("5");

    private final SubscriptionQueryAdapter subscriptionQueryPort;
    private final SubscriptionPriceQueryAdapter subscriptionPriceQueryPort;
    private final RealTransactionQueryAdapter realTransactionQueryPort;
    private final CompetitionRateRepository competitionRateRepository;
    private final AddressHelper addressHelper;
    private final HouseTypeComparisonBuilder comparisonBuilder;

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
                        .priceBadge(computePriceBadge(s))
                        .build())
                .toList();

        List<SubscriptionCardView> upcomingCards = filterUpcomingSubscriptions(activeUpcoming).stream()
                .sorted(Comparator.comparing(Subscription::getReceiptStartDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(s -> SubscriptionCardView.builder()
                        .subscription(s)
                        .priceBadge(computePriceBadge(s))
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

    private PriceBadge computePriceBadge(Subscription subscription) {
        // LH이거나 houseManageNo/address 없음 → UNKNOWN
        if (subscription.getSource() != null && subscription.getSource().toUpperCase().contains("LH")) {
            return PriceBadge.UNKNOWN;
        }
        if (subscription.getHouseManageNo() == null || subscription.getAddress() == null) {
            return PriceBadge.UNKNOWN;
        }

        // 분양가 조회
        List<SubscriptionPrice> prices = subscriptionPriceQueryPort.findByHouseManageNo(subscription.getHouseManageNo());
        if (prices.isEmpty()) {
            return PriceBadge.UNKNOWN;
        }

        // 대표 평형 선택: 84㎡ 우선, 없으면 공급세대 가장 많은 타입
        SubscriptionPrice representative = selectRepresentativePrice(prices);
        if (representative == null || representative.getTopAmount() == null) {
            return PriceBadge.UNKNOWN;
        }

        BigDecimal area = comparisonBuilder.extractAreaFromHouseType(representative.getHouseType());
        if (area == null) {
            return PriceBadge.UNKNOWN;
        }

        // 주소에서 lawdCd/dongName 파싱
        String lawdCd = addressHelper.extractLawdCd(subscription.getAddress());
        String dongName = addressHelper.extractDongName(subscription.getAddress());
        if (lawdCd == null) {
            return PriceBadge.UNKNOWN;
        }

        // DB 캐시만 조회 (API 호출 X)
        List<RealTransaction> allTransactions = realTransactionQueryPort.findByLawdCd(lawdCd);
        List<RealTransaction> dongTransactions = addressHelper.filterByDongName(allTransactions, dongName);

        // 5년 내 준공 필터
        int newBuildYearThreshold = LocalDate.now().getYear() - 4;
        List<RealTransaction> newBuildTx = dongTransactions.stream()
                .filter(t -> t.getBuildYear() != null && t.getBuildYear() >= newBuildYearThreshold)
                .toList();
        if (newBuildTx.isEmpty()) {
            return PriceBadge.UNKNOWN;
        }

        // 유사 면적(±5㎡) 거래 필터
        BigDecimal minArea = area.subtract(AREA_TOLERANCE);
        BigDecimal maxArea = area.add(AREA_TOLERANCE);
        List<Long> similarAmounts = newBuildTx.stream()
                .filter(t -> t.getExclusiveArea() != null)
                .filter(t -> t.getExclusiveArea().compareTo(minArea) >= 0
                          && t.getExclusiveArea().compareTo(maxArea) <= 0)
                .map(RealTransaction::getDealAmount)
                .sorted()
                .toList();

        if (similarAmounts.isEmpty()) {
            return PriceBadge.UNKNOWN;
        }

        // 중앙값 계산
        long median;
        int size = similarAmounts.size();
        if (size % 2 == 0) {
            median = (similarAmounts.get(size / 2 - 1) + similarAmounts.get(size / 2)) / 2;
        } else {
            median = similarAmounts.get(size / 2);
        }

        // 비교: 분양가 < 중앙값 * 0.95 → CHEAP, > 중앙값 * 1.05 → EXPENSIVE
        long supplyPrice = representative.getTopAmount();
        if (supplyPrice < median * 0.95) {
            return PriceBadge.CHEAP;
        } else if (supplyPrice > median * 1.05) {
            return PriceBadge.EXPENSIVE;
        } else {
            return PriceBadge.UNKNOWN;
        }
    }

    private SubscriptionPrice selectRepresentativePrice(List<SubscriptionPrice> prices) {
        BigDecimal target84 = new BigDecimal("84");

        // 84㎡에 가장 가까운 타입 찾기
        SubscriptionPrice closest84 = prices.stream()
                .filter(p -> comparisonBuilder.extractAreaFromHouseType(p.getHouseType()) != null)
                .filter(p -> {
                    BigDecimal area = comparisonBuilder.extractAreaFromHouseType(p.getHouseType());
                    return area.subtract(target84).abs().compareTo(AREA_TOLERANCE) <= 0;
                })
                .min(Comparator.comparing(p -> comparisonBuilder.extractAreaFromHouseType(p.getHouseType()).subtract(target84).abs()))
                .orElse(null);

        if (closest84 != null) {
            return closest84;
        }

        // 없으면 공급세대 가장 많은 타입
        return prices.stream()
                .max(Comparator.comparingInt(SubscriptionPrice::getTotalSupplyCount))
                .orElse(null);
    }
}
