package com.yunbok.houseping.controller.web;

import com.yunbok.houseping.core.domain.Subscription;
import com.yunbok.houseping.core.service.subscription.SubscriptionSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class SitemapController {

    private static final String BASE_URL = "https://house-ping.com";

    private final SubscriptionSearchService subscriptionSearchService;

    @GetMapping(value = "/sitemap.xml", produces = "application/xml;charset=UTF-8")
    @ResponseBody
    public String sitemap() {
        String today = LocalDate.now().toString();
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        // 홈페이지
        appendUrl(sb, BASE_URL + "/home", today, "daily", "1.0");
        appendUrl(sb, BASE_URL + "/home/calendar", today, "weekly", "0.8");
        appendUrl(sb, BASE_URL + "/home/calculator", today, "monthly", "0.8");

        // 가이드 페이지
        appendUrl(sb, BASE_URL + "/home/guide", today, "weekly", "0.8");
        for (GuideSlug guide : GuideSlug.values()) {
            appendUrl(sb, BASE_URL + "/home/guide/" + guide.getSlug(), today, "monthly", "0.7");
        }

        // 월별 페이지: 최근 12개월
        YearMonth now = YearMonth.now();
        for (int i = -6; i <= 6; i++) {
            YearMonth ym = now.plusMonths(i);
            String path = String.format("/home/%d/%d", ym.getYear(), ym.getMonthValue());
            appendUrl(sb, BASE_URL + path, today, "weekly", "0.8");
        }

        // 분석 페이지: 전체 청약
        List<Subscription> allSubscriptions = subscriptionSearchService.findAll();
        for (Subscription s : allSubscriptions) {
            if (s.getId() != null) {
                String lastmod = s.getReceiptStartDate() != null
                        ? s.getReceiptStartDate().toString() : today;
                appendUrl(sb, BASE_URL + "/home/analysis/" + s.getId(), lastmod, "weekly", "0.6");
            }
        }

        sb.append("</urlset>");
        return sb.toString();
    }

    private void appendUrl(StringBuilder sb, String loc, String lastmod, String changefreq, String priority) {
        sb.append("  <url>\n");
        sb.append("    <loc>").append(loc).append("</loc>\n");
        sb.append("    <lastmod>").append(lastmod).append("</lastmod>\n");
        sb.append("    <changefreq>").append(changefreq).append("</changefreq>\n");
        sb.append("    <priority>").append(priority).append("</priority>\n");
        sb.append("  </url>\n");
    }
}
