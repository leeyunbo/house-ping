package com.yunbok.houseping.controller.web;

import com.yunbok.houseping.core.service.realtransaction.RealTransactionCollectionService;
import com.yunbok.houseping.core.service.subscription.SubscriptionManagementService;
import com.yunbok.houseping.support.dto.SchedulerResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 데이터 수집 컨트롤러 (MASTER 전용)
 */
@Slf4j
@Controller
@RequestMapping("/admin/system")
@RequiredArgsConstructor
public class AdminDataCollectionController {

    private final SubscriptionManagementService subscriptionManagementService;
    private final RealTransactionCollectionService realTransactionCollectionService;

    @PostMapping("/collect-price-data")
    public String collectPriceData(RedirectAttributes redirectAttributes) {
        try {
            log.info("[admin.collect] 분양가 데이터 수집 시작");
            SchedulerResult result = subscriptionManagementService.collectPriceData();
            String message = String.format("분양가 수집 완료 - %s", result.summary());
            redirectAttributes.addFlashAttribute("message", message);
        } catch (Exception e) {
            log.error("[admin.collect] 분양가 수집 실패", e);
            redirectAttributes.addFlashAttribute("error", "분양가 수집 실패: " + e.getMessage());
        }
        return "redirect:/admin/system";
    }

    @PostMapping("/collect-real-transactions")
    public String collectRealTransactions(RedirectAttributes redirectAttributes) {
        try {
            log.info("[admin.collect] 실거래가 수집 시작");
            realTransactionCollectionService.collectRealTransactions();
            redirectAttributes.addFlashAttribute("message", "실거래가 수집이 완료되었습니다.");
        } catch (Exception e) {
            log.error("[admin.collect] 실거래가 수집 실패", e);
            redirectAttributes.addFlashAttribute("error", "실거래가 수집 실패: " + e.getMessage());
        }
        return "redirect:/admin/system";
    }
}
