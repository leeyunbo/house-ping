package com.yunbok.houseping.core.service.realtransaction;

import com.yunbok.houseping.core.domain.SubscriptionSource;
import com.yunbok.houseping.core.service.region.RegionCodeService;
import com.yunbok.houseping.entity.SubscriptionEntity;
import com.yunbok.houseping.core.port.RealTransactionFetchPort;
import com.yunbok.houseping.repository.SubscriptionRepository;
import com.yunbok.houseping.support.util.ApiRateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RealTransactionCollectionService {

    private final SubscriptionRepository subscriptionRepository;
    private final RegionCodeService regionCodeService;
    private final RealTransactionFetchPort realTransactionFetchPort;

    public void collectRealTransactions() {
        log.info("[실거래가] 수집 시작");

        LocalDate today = LocalDate.now();

        List<SubscriptionEntity> subscriptions = subscriptionRepository.findAll().stream()
                .filter(s -> SubscriptionSource.APPLYHOME.matches(s.getSource()))
                .filter(s -> s.getReceiptStartDate() != null)
                .filter(s -> {
                    LocalDate endDate = s.getReceiptEndDate() != null ? s.getReceiptEndDate() : s.getReceiptStartDate();
                    return !endDate.isBefore(today);
                })
                .toList();

        log.info("[실거래가] 대상 청약: {}건", subscriptions.size());

        Set<String> lawdCodes = new HashSet<>();
        for (SubscriptionEntity subscription : subscriptions) {
            Optional<String> lawdCd = regionCodeService.findLawdCdByAddress(subscription.getAddress());
            lawdCd.ifPresent(lawdCodes::add);
        }

        log.info("[실거래가] 수집 대상 지역: {}개", lawdCodes.size());

        int successCount = 0;
        int failCount = 0;

        for (String lawdCd : lawdCodes) {
            try {
                realTransactionFetchPort.fetchAndCacheRecentTransactions(lawdCd, 6);
                successCount++;
                ApiRateLimiter.delay(200);
            } catch (Exception e) {
                failCount++;
                log.warn("[실거래가] {} 수집 실패: {}", lawdCd, e.getMessage());
            }
        }

        log.info("[실거래가] 수집 완료 - 성공: {}개 지역, 실패: {}개 지역", successCount, failCount);
    }
}
