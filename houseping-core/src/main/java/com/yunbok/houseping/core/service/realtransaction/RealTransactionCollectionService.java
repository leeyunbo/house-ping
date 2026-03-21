package com.yunbok.houseping.core.service.realtransaction;

import com.yunbok.houseping.core.domain.RealTransaction;
import com.yunbok.houseping.core.domain.Subscription;
import com.yunbok.houseping.core.domain.SubscriptionSource;
import com.yunbok.houseping.core.port.ApartmentGeocodingPort;
import com.yunbok.houseping.core.port.SubscriptionPersistencePort;
import com.yunbok.houseping.core.service.region.RegionCodeService;
import com.yunbok.houseping.core.port.RealTransactionFetchPort;
import com.yunbok.houseping.support.util.ApiRateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RealTransactionCollectionService {

    private final SubscriptionPersistencePort subscriptionPersistencePort;
    private final RegionCodeService regionCodeService;
    private final RealTransactionFetchPort realTransactionFetchPort;
    private final ApartmentGeocodingPort geocodingPort;

    public void collectRealTransactions() {
        log.info("[collect.realTransaction] 수집 시작");

        LocalDate today = LocalDate.now();

        List<Subscription> subscriptions = subscriptionPersistencePort.findAll().stream()
                .filter(s -> SubscriptionSource.APPLYHOME.matches(s.getSource()))
                .filter(s -> s.getReceiptStartDate() != null)
                .filter(s -> {
                    LocalDate endDate = s.getReceiptEndDate() != null ? s.getReceiptEndDate() : s.getReceiptStartDate();
                    return !endDate.isBefore(today);
                })
                .toList();

        log.info("[collect.realTransaction] 대상 청약: {}건", subscriptions.size());

        Set<String> lawdCodes = new HashSet<>();
        for (Subscription subscription : subscriptions) {
            Optional<String> lawdCd = regionCodeService.findLawdCdByAddress(subscription.getAddress());
            lawdCd.ifPresent(lawdCodes::add);
        }

        log.info("[collect.realTransaction] 수집 대상 지역: {}개", lawdCodes.size());

        List<RealTransaction> allCollected = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;

        for (String lawdCd : lawdCodes) {
            try {
                List<RealTransaction> transactions = realTransactionFetchPort.fetchAndCacheRecentTransactions(lawdCd, 6);
                allCollected.addAll(transactions);
                successCount++;
                ApiRateLimiter.delay(200);
            } catch (Exception e) {
                failCount++;
                log.warn("[collect.realTransaction] {} 수집 실패: {}", lawdCd, e.getMessage());
            }
        }

        log.info("[collect.realTransaction] 수집 완료 - 성공: {}개 지역, 실패: {}개 지역", successCount, failCount);

        geocodeApartments(allCollected);
    }

    private void geocodeApartments(List<RealTransaction> transactions) {
        // 아파트별 중복 제거 (aptName + lawdCd)
        Map<String, RealTransaction> unique = transactions.stream()
                .collect(Collectors.toMap(
                        t -> t.getAptName() + ":" + t.getLawdCd(),
                        t -> t,
                        (a, b) -> a
                ));

        log.info("[collect.realTransaction] 지오코딩 대상 아파트: {}건", unique.size());

        for (RealTransaction tx : unique.values()) {
            try {
                geocodingPort.geocodeIfAbsent(tx.getAptName(), tx.getLawdCd(), tx.getDongName(), tx.getJibun());
                ApiRateLimiter.delay(100);
            } catch (Exception e) {
                log.warn("[collect.realTransaction] 지오코딩 실패: {} - {}", tx.getAptName(), e.getMessage());
            }
        }
    }
}
