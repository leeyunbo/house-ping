package com.yunbok.houseping.controller.web;

import com.yunbok.houseping.service.dto.AdminSubscriptionDto;
import com.yunbok.houseping.service.dto.AdminSubscriptionSearchCriteria;
import com.yunbok.houseping.support.dto.CalendarEventDto;
import com.yunbok.houseping.support.dto.SyncResult;
import com.yunbok.houseping.service.AdminSubscriptionService;
import com.yunbok.houseping.core.service.subscription.SubscriptionManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequestMapping("/admin/subscriptions")
@RequiredArgsConstructor
public class AdminSubscriptionController {

    private final AdminSubscriptionService queryService;
    private final SubscriptionManagementService managementUseCase;

    @Value("${kakao.map.app-key:}")
    private String kakaoMapAppKey;

    @GetMapping
    public String list(AdminSubscriptionSearchCriteria criteria, Model model) {
        model.addAttribute("resultPage", queryService.search(criteria));
        model.addAttribute("search", criteria);
        model.addAttribute("areas", queryService.availableAreas());
        model.addAttribute("houseTypes", queryService.availableHouseTypes());
        model.addAttribute("sources", queryService.availableSources());
        model.addAttribute("kakaoMapAppKey", kakaoMapAppKey);
        return "admin/subscriptions/list";
    }

    @GetMapping("/calendar")
    public String calendar(Model model) {
        model.addAttribute("kakaoMapAppKey", kakaoMapAppKey);
        return "admin/subscriptions/calendar";
    }

    @GetMapping("/calendar/events")
    @ResponseBody
    public ResponseEntity<List<CalendarEventDto>> calendarEvents(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(queryService.getCalendarEvents(start, end));
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<AdminSubscriptionDto> getById(@PathVariable Long id) {
        return queryService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/sync")
    public String sync(RedirectAttributes redirectAttributes) {
        try {
            SyncResult result = managementUseCase.sync();
            redirectAttributes.addFlashAttribute("message",
                    String.format("동기화 완료: 신규 %d건, 업데이트 %d건",
                            result.inserted(), result.updated()));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "동기화 실패: " + e.getMessage());
        }
        return "redirect:/admin/subscriptions";
    }

    @PostMapping("/{id}/notification")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleNotification(@PathVariable Long id) {
        boolean enabled = queryService.toggleNotification(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "enabled", enabled,
                "message", enabled ? "알림이 설정되었습니다." : "알림이 해제되었습니다."
        ));
    }

    @DeleteMapping("/{id}/notification")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeNotification(@PathVariable Long id) {
        queryService.removeNotification(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "알림이 해제되었습니다."
        ));
    }

    @PostMapping("/notifications/bulk")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> bulkEnableNotifications(@RequestBody BulkNotificationRequest request) {
        int count = queryService.enableNotifications(request.ids());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", count,
                "message", count + "개 청약에 알림이 설정되었습니다."
        ));
    }

    public record BulkNotificationRequest(List<Long> ids) {}

    @GetMapping("/{id}/prices")
    @ResponseBody
    public ResponseEntity<List<AdminSubscriptionService.PriceDto>> getPrices(@PathVariable Long id) {
        return ResponseEntity.ok(queryService.getPrices(id));
    }

    @GetMapping("/{id}/market-analysis")
    @ResponseBody
    public ResponseEntity<AdminSubscriptionService.MarketAnalysisDto> getMarketAnalysis(@PathVariable Long id) {
        return ResponseEntity.ok(queryService.getMarketAnalysis(id));
    }
}
