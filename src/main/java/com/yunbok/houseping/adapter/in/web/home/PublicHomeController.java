package com.yunbok.houseping.adapter.in.web.home;

import com.yunbok.houseping.domain.model.Subscription;
import com.yunbok.houseping.domain.model.SubscriptionAnalysisResult;
import com.yunbok.houseping.domain.model.SubscriptionPrice;
import com.yunbok.houseping.domain.port.in.SubscriptionAnalysisUseCase;
import com.yunbok.houseping.domain.port.in.SubscriptionQueryUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 공개 홈페이지 컨트롤러
 * 비로그인 사용자도 접근 가능
 */
@Slf4j
@Controller
@RequestMapping("/home")
@RequiredArgsConstructor
public class PublicHomeController {

    private final SubscriptionQueryUseCase subscriptionQueryUseCase;
    private final SubscriptionAnalysisUseCase subscriptionAnalysisUseCase;

    @Value("${kakao.map.app-key:}")
    private String kakaoMapAppKey;

    /**
     * 랜딩 페이지 - 청약 목록
     */
    @GetMapping
    public String index(
            @RequestParam(required = false) String area,
            @RequestParam(required = false) String status,
            Model model) {

        // 청약 목록 조회 (ApplyHome만, 서울/경기, 접수중 + 예정)
        List<Subscription> subscriptions = subscriptionQueryUseCase.findActiveAndUpcomingSubscriptions(area);

        // 상태별 분류
        List<Subscription> activeSubscriptions;
        List<Subscription> upcomingSubscriptions;

        if ("active".equals(status)) {
            activeSubscriptions = subscriptionQueryUseCase.filterActiveSubscriptions(subscriptions);
            upcomingSubscriptions = List.of();
        } else if ("upcoming".equals(status)) {
            activeSubscriptions = List.of();
            upcomingSubscriptions = subscriptionQueryUseCase.filterUpcomingSubscriptions(subscriptions);
        } else {
            activeSubscriptions = subscriptionQueryUseCase.filterActiveSubscriptions(subscriptions);
            upcomingSubscriptions = subscriptionQueryUseCase.filterUpcomingSubscriptions(subscriptions);
        }

        model.addAttribute("activeSubscriptions", activeSubscriptions);
        model.addAttribute("upcomingSubscriptions", upcomingSubscriptions);
        model.addAttribute("selectedArea", area);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("areas", List.of("서울", "경기"));

        return "home/index";
    }

    /**
     * 청약 분석 페이지
     */
    @GetMapping("/analysis/{id}")
    public String analysis(@PathVariable Long id, Model model) {
        try {
            SubscriptionAnalysisResult analysis = subscriptionAnalysisUseCase.analyze(id);
            Subscription subscription = analysis.getSubscription();

            model.addAttribute("analysis", analysis);
            model.addAttribute("subscription", subscription);
            model.addAttribute("marketAnalysis", analysis.getMarketAnalysis());
            model.addAttribute("recentTransactions", analysis.getRecentTransactions());
            model.addAttribute("houseTypeComparisons", analysis.getHouseTypeComparisons());
            model.addAttribute("dongName", analysis.getDongName());

            // 분양가 정보
            List<SubscriptionPrice> prices = analysis.getPrices();
            model.addAttribute("prices", prices);

            // 상태 라벨
            model.addAttribute("statusLabel", subscription.getStatusLabel());

            // 카카오맵 키
            model.addAttribute("kakaoMapAppKey", kakaoMapAppKey);

            return "home/analysis";
        } catch (IllegalArgumentException e) {
            log.warn("청약 분석 실패: {}", e.getMessage());
            return "redirect:/home";
        }
    }
}
