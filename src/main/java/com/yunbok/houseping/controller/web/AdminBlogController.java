package com.yunbok.houseping.controller.web;

import com.yunbok.houseping.core.service.blog.BlogPublishService;
import com.yunbok.houseping.core.service.blog.WeeklyBlogContentService;
import com.yunbok.houseping.support.dto.BlogContentResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Base64;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/blog")
@RequiredArgsConstructor
public class AdminBlogController {

    private final WeeklyBlogContentService weeklyBlogContentService;
    private final BlogPublishService blogPublishService;

    @GetMapping("/weekly")
    public String weeklyPreview(@RequestParam(defaultValue = "5") int topN, Model model) {
        BlogContentResult result = weeklyBlogContentService.generateWeeklyContent(topN);

        var entriesWithBase64 = result.getEntries().stream()
                .map(e -> new CardEntryView(
                        e.getSubscriptionId(),
                        e.getHouseName(),
                        e.getRank(),
                        e.getNarrativeText(),
                        Base64.getEncoder().encodeToString(e.getCardImage())
                ))
                .collect(Collectors.toList());

        model.addAttribute("result", result);
        model.addAttribute("entries", entriesWithBase64);
        model.addAttribute("topN", topN);
        return "admin/blog/weekly";
    }

    @GetMapping("/card/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> downloadCard(
            @PathVariable Long id,
            @RequestParam(defaultValue = "5") int topN) {
        BlogContentResult result = weeklyBlogContentService.generateWeeklyContent(topN);

        return result.getEntries().stream()
                .filter(e -> e.getSubscriptionId().equals(id))
                .findFirst()
                .map(e -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"card-" + e.getSubscriptionId() + ".png\"")
                        .contentType(MediaType.IMAGE_PNG)
                        .body(e.getCardImage()))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/weekly/text")
    @ResponseBody
    public ResponseEntity<String> weeklyText(@RequestParam(defaultValue = "5") int topN) {
        BlogContentResult result = weeklyBlogContentService.generateWeeklyContent(topN);
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "plain", java.nio.charset.StandardCharsets.UTF_8))
                .body(result.getBlogText());
    }

    @PostMapping("/publish")
    public String publish(@RequestParam(defaultValue = "5") int topN,
                          HttpServletRequest request,
                          RedirectAttributes redirectAttributes) {
        String baseUrl = request.getScheme() + "://" + request.getServerName();
        if (request.getServerPort() != 80 && request.getServerPort() != 443) {
            baseUrl += ":" + request.getServerPort();
        }
        blogPublishService.publishWeekly(topN, baseUrl);
        redirectAttributes.addFlashAttribute("message", "블로그 포스트가 발행되었습니다.");
        return "redirect:/admin/blog/posts";
    }

    @PostMapping("/generate-ai")
    public String generateAi(@RequestParam(defaultValue = "5") int topN,
                              RedirectAttributes redirectAttributes) {
        try {
            blogPublishService.saveDraftWithAi(topN);
            redirectAttributes.addFlashAttribute("message", "AI 블로그 DRAFT가 생성되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "AI 블로그 생성에 실패했습니다: " + e.getMessage());
        }
        return "redirect:/admin/blog/posts";
    }

    @GetMapping("/posts")
    public String posts(Model model) {
        model.addAttribute("posts", blogPublishService.findAll());
        return "admin/blog/posts";
    }

    @PostMapping("/posts/{id}/unpublish")
    public String unpublish(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        blogPublishService.unpublish(id);
        redirectAttributes.addFlashAttribute("message", "게시가 중단되었습니다.");
        return "redirect:/admin/blog/posts";
    }

    @PostMapping("/posts/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        blogPublishService.delete(id);
        redirectAttributes.addFlashAttribute("message", "포스트가 삭제되었습니다.");
        return "redirect:/admin/blog/posts";
    }

    public record CardEntryView(Long subscriptionId, String houseName, int rank,
                                String narrativeText, String cardImageBase64) {}
}
