package com.yunbok.houseping.controller.web;

import com.yunbok.houseping.support.dto.PublicCalendarEventDto;
import com.yunbok.houseping.core.service.calendar.PublicCalendarService;
import com.yunbok.houseping.core.domain.Subscription;
import com.yunbok.houseping.support.dto.SubscriptionAnalysisResult;
import com.yunbok.houseping.core.service.subscription.SubscriptionAnalysisService;
import com.yunbok.houseping.core.service.subscription.SubscriptionSearchService;
import lombok.RequiredArgsConstructor;
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
@Controller
@RequestMapping("/home")
@RequiredArgsConstructor
public class PublicHomeController {

    private final SubscriptionSearchService subscriptionSearchService;
    private final SubscriptionAnalysisService subscriptionAnalysisService;
    private final PublicCalendarService publicCalendarService;

    @Value("${kakao.map.app-key:}")
    private String kakaoMapAppKey;

    /**
     * 랜딩 페이지 - 청약 목록
     */
    @GetMapping
    public String index(
            @RequestParam(required = false) String area,
            Model model) {

        model.addAttribute("home", subscriptionSearchService.getHomeData(area));
        model.addAttribute("kakaoMapAppKey", kakaoMapAppKey);
        model.addAttribute("current", YearMonth.now());
        model.addAttribute("canonicalPath", "/home");

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

        model.addAttribute("monthly", subscriptionSearchService.getMonthlyData(year, month));

        YearMonth current = YearMonth.of(year, month);
        model.addAttribute("current", current);
        model.addAttribute("prev", current.minusMonths(1));
        model.addAttribute("next", current.plusMonths(1));
        model.addAttribute("canonicalPath", "/home/" + year + "/" + month);

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
     * 청약 캘린더 페이지
     */
    @GetMapping("/calendar")
    public String calendar(Model model) {
        model.addAttribute("canonicalPath", "/home/calendar");
        return "home/calendar";
    }

    /**
     * 청약 가점 계산기
     */
    @GetMapping("/calculator")
    public String calculator(Model model) {
        model.addAttribute("canonicalPath", "/home/calculator");
        return "home/calculator";
    }

    /**
     * 청약 가이드 목록
     */
    @GetMapping("/guide")
    public String guideIndex(Model model) {
        model.addAttribute("canonicalPath", "/home/guide");
        return "home/guide/index";
    }

    /**
     * 청약 가이드 상세
     */
    @GetMapping("/guide/{slug}")
    public String guideDetail(@PathVariable String slug, Model model) {
        if (!GuideSlug.isValid(slug)) {
            return "redirect:/home/guide";
        }
        model.addAttribute("canonicalPath", "/home/guide/" + slug);
        return "home/guide/" + slug;
    }

    /**
     * 청약 분석 페이지
     */
    @GetMapping("/analysis/{id}")
    public String analysis(@PathVariable Long id, Model model) {
        SubscriptionAnalysisResult analysis = subscriptionAnalysisService.analyze(id);
        Subscription subscription = analysis.getSubscription();

        model.addAttribute("analysis", analysis);
        model.addAttribute("subscription", subscription);
        model.addAttribute("canonicalPath", "/home/analysis/" + id);
        model.addAttribute("kakaoMapAppKey", kakaoMapAppKey);

        return "home/analysis";
    }
}
