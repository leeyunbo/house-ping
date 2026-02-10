package com.yunbok.houseping.controller.web;

import com.yunbok.houseping.support.dto.PublicCalendarEventDto;
import com.yunbok.houseping.core.service.calendar.PublicCalendarService;
import com.yunbok.houseping.core.domain.Subscription;
import com.yunbok.houseping.support.dto.SubscriptionAnalysisResult;
import com.yunbok.houseping.core.domain.SubscriptionPrice;
import com.yunbok.houseping.core.service.subscription.SubscriptionAnalysisService;
import com.yunbok.houseping.core.service.subscription.SubscriptionQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.YearMonth;
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

    private final SubscriptionQueryService subscriptionQueryUseCase;
    private final SubscriptionAnalysisService subscriptionAnalysisUseCase;
    private final PublicCalendarService publicCalendarService;

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
        model.addAttribute("kakaoMapAppKey", kakaoMapAppKey);

        // 월별/지역별 바로가기용 데이터
        YearMonth now = YearMonth.now();
        model.addAttribute("currentYear", now.getYear());
        model.addAttribute("currentMonth", now.getMonthValue());
        YearMonth prev = now.minusMonths(1);
        YearMonth next = now.plusMonths(1);
        model.addAttribute("prevYear", prev.getYear());
        model.addAttribute("prevMonth", prev.getMonthValue());
        model.addAttribute("nextYear", next.getYear());
        model.addAttribute("nextMonth", next.getMonthValue());
        return "home/index";
    }

    /**
     * 월별 청약 일정 (SEO 페이지)
     */
    @GetMapping("/{year}/{month}")
    public String monthly(@PathVariable int year, @PathVariable int month, Model model) {
        if (month < 1 || month > 12) {
            return "redirect:/home";
        }

        List<Subscription> subscriptions = subscriptionQueryUseCase.findByMonth(year, month);
        List<Subscription> activeSubscriptions = subscriptionQueryUseCase.filterActiveSubscriptions(subscriptions);
        List<Subscription> upcomingSubscriptions = subscriptionQueryUseCase.filterUpcomingSubscriptions(subscriptions);
        List<Subscription> closedSubscriptions = subscriptions.stream()
                .filter(s -> s.getStatus() == com.yunbok.houseping.core.domain.SubscriptionStatus.CLOSED)
                .toList();

        model.addAttribute("subscriptions", subscriptions);
        model.addAttribute("activeSubscriptions", activeSubscriptions);
        model.addAttribute("upcomingSubscriptions", upcomingSubscriptions);
        model.addAttribute("closedSubscriptions", closedSubscriptions);
        model.addAttribute("year", year);
        model.addAttribute("month", month);

        YearMonth current = YearMonth.of(year, month);
        YearMonth prev = current.minusMonths(1);
        YearMonth next = current.plusMonths(1);
        model.addAttribute("prevYear", prev.getYear());
        model.addAttribute("prevMonth", prev.getMonthValue());
        model.addAttribute("nextYear", next.getYear());
        model.addAttribute("nextMonth", next.getMonthValue());

        String title = year + "년 " + month + "월 청약 일정";
        String description = year + "년 " + month + "월 아파트 청약 일정, 접수 기간, 공급 세대수 정보";
        model.addAttribute("pageTitle", title);
        model.addAttribute("pageDescription", description);

        return "home/monthly";
    }

    /**
     * 공개 캘린더 이벤트 API
     * 청약Home + LH 모두 표시, 알림 기능 제외
     */
    @GetMapping("/calendar/events")
    @ResponseBody
    public ResponseEntity<List<PublicCalendarEventDto>> getCalendarEvents(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(publicCalendarService.getCalendarEvents(start, end));
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
