package com.yunbok.houseping.infrastructure.adapter.inbound.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

/**
 * 간단한 관리자 UI 컨트롤러.
 */
@Controller
@RequestMapping("/admin/subscriptions")
@RequiredArgsConstructor
public class AdminSubscriptionController {

    private final AdminSubscriptionQueryService queryService;

    @GetMapping
    public String list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model
    ) {
        var criteria = new AdminSubscriptionSearchCriteria(
                keyword,
                area,
                source,
                startDate,
                endDate,
                page,
                size
        );

        var resultPage = queryService.search(criteria);

        model.addAttribute("resultPage", resultPage);
        model.addAttribute("search", criteria);
        model.addAttribute("areas", queryService.availableAreas());
        model.addAttribute("sources", queryService.availableSources());

        return "admin/subscriptions/list";
    }
}
