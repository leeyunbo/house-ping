package com.yunbok.houseping.controller.web;

import com.yunbok.houseping.infrastructure.api.ApplyhomeApiClient;
import com.yunbok.houseping.core.domain.SubscriptionSource;
import com.yunbok.houseping.entity.SubscriptionEntity;
import com.yunbok.houseping.repository.SubscriptionPriceRepository;
import com.yunbok.houseping.repository.SubscriptionRepository;
import com.yunbok.houseping.scheduler.RealTransactionScheduler;
import com.yunbok.houseping.support.util.ApiRateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 데이터 수집 컨트롤러 (MASTER 전용)
 */
@Slf4j
@Controller
@RequestMapping("/admin/system")
@RequiredArgsConstructor
public class AdminDataCollectionController {

    private static final LocalDate PRICE_COLLECTION_START_DATE = LocalDate.of(2025, 1, 1);

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPriceRepository subscriptionPriceRepository;
    private final ApplyhomeApiClient applyhomeApiAdapter;
    private final RealTransactionScheduler realTransactionScheduler;

    /**
     * ApplyHome 청약 분양가 수집
     */
    @PostMapping("/collect-price-data")
    public String collectPriceData(RedirectAttributes redirectAttributes) {
        try {
            log.info("[데이터 수집] 분양가 데이터 수집 시작");

            List<SubscriptionEntity> subscriptions = findPriceCollectionTargets();
            log.info("[데이터 수집] 분양가 수집 대상: {}건", subscriptions.size());

            CollectionResult result = collectPriceDetails(subscriptions);

            String message = String.format("분양가 수집 완료 - 성공: %d건, 실패: %d건",
                    result.successCount(), result.failCount());
            log.info("[데이터 수집] {}", message);
            redirectAttributes.addFlashAttribute("message", message);

        } catch (Exception e) {
            log.error("[데이터 수집] 분양가 수집 실패", e);
            redirectAttributes.addFlashAttribute("error", "분양가 수집 실패: " + e.getMessage());
        }
        return "redirect:/admin/system";
    }

    /**
     * 실거래가 데이터 수집 (접수예정 청약 지역)
     */
    @PostMapping("/collect-real-transactions")
    public String collectRealTransactions(RedirectAttributes redirectAttributes) {
        try {
            log.info("[데이터 수집] 실거래가 수집 시작");
            realTransactionScheduler.collectRealTransactions();
            redirectAttributes.addFlashAttribute("message", "실거래가 수집이 완료되었습니다.");
        } catch (Exception e) {
            log.error("[데이터 수집] 실거래가 수집 실패", e);
            redirectAttributes.addFlashAttribute("error", "실거래가 수집 실패: " + e.getMessage());
        }
        return "redirect:/admin/system";
    }

    private List<SubscriptionEntity> findPriceCollectionTargets() {
        return subscriptionRepository.findAll().stream()
                .filter(s -> SubscriptionSource.APPLYHOME.matches(s.getSource()))
                .filter(s -> s.getHouseManageNo() != null && !s.getHouseManageNo().isEmpty())
                .filter(s -> s.getReceiptStartDate() != null && !s.getReceiptStartDate().isBefore(PRICE_COLLECTION_START_DATE))
                .filter(s -> !subscriptionPriceRepository.existsByHouseManageNo(s.getHouseManageNo()))
                .toList();
    }

    private CollectionResult collectPriceDetails(List<SubscriptionEntity> subscriptions) {
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (SubscriptionEntity subscription : subscriptions) {
            try {
                applyhomeApiAdapter.fetchAndSavePriceDetails(
                        subscription.getHouseManageNo(),
                        subscription.getPblancNo(),
                        subscription.getHouseType()
                );
                successCount.incrementAndGet();
                ApiRateLimiter.delay(100);
            } catch (Exception e) {
                failCount.incrementAndGet();
                log.warn("[분양가] {} 수집 실패: {}", subscription.getHouseName(), e.getMessage());
            }
        }

        return new CollectionResult(successCount.get(), failCount.get());
    }

    private record CollectionResult(int successCount, int failCount) {}
}
