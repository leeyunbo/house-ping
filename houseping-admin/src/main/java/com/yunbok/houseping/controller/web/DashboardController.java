package com.yunbok.houseping.controller.web;

import com.yunbok.houseping.service.DashboardQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardQueryService dashboardQueryService;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("stats", dashboardQueryService.getStatistics());
        return "admin/dashboard";
    }
}
