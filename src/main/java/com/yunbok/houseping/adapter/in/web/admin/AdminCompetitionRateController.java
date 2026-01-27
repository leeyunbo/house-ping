package com.yunbok.houseping.adapter.in.web.admin;

import com.yunbok.houseping.domain.service.CompetitionRateCollectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/competition-rates")
@RequiredArgsConstructor
public class AdminCompetitionRateController {

    private final AdminCompetitionRateQueryService queryService;
    private final CompetitionRateCollectorService collectorService;

    @GetMapping
    public String list(AdminCompetitionRateSearchCriteria criteria, Model model) {
        model.addAttribute("resultPage", queryService.search(criteria));
        model.addAttribute("search", criteria);
        model.addAttribute("houseTypes", queryService.availableHouseTypes());
        model.addAttribute("areas", queryService.availableAreas());
        model.addAttribute("stats", queryService.getStats());
        return "admin/competition-rates/list";
    }

    @PostMapping("/collect")
    public String collect(RedirectAttributes redirectAttributes) {
        try {
            int count = collectorService.collect();
            redirectAttributes.addFlashAttribute("message", count + "건의 경쟁률 데이터를 수집했습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "수집 실패: " + e.getMessage());
        }
        return "redirect:/admin/competition-rates";
    }

    @PostMapping("/delete-all")
    public String deleteAll(RedirectAttributes redirectAttributes) {
        try {
            queryService.deleteAll();
            redirectAttributes.addFlashAttribute("message", "모든 경쟁률 데이터가 삭제되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "삭제 실패: " + e.getMessage());
        }
        return "redirect:/admin/competition-rates";
    }
}
