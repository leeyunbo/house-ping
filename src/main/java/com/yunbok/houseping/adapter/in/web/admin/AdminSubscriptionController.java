package com.yunbok.houseping.adapter.in.web.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/subscriptions")
@RequiredArgsConstructor
public class AdminSubscriptionController {

    private final AdminSubscriptionQueryService queryService;

    @GetMapping
    public String list(AdminSubscriptionSearchCriteria criteria, Model model) {
        model.addAttribute("resultPage", queryService.search(criteria));
        model.addAttribute("search", criteria);
        model.addAttribute("areas", queryService.availableAreas());
        model.addAttribute("sources", queryService.availableSources());
        return "admin/subscriptions/list";
    }
}
