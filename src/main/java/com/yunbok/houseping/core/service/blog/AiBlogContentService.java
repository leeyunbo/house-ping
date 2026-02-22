package com.yunbok.houseping.core.service.blog;

import com.yunbok.houseping.adapter.api.ClaudeApiAdapter;
import com.yunbok.houseping.core.service.blog.WeeklyBlogContentService.ScoredEntry;
import com.yunbok.houseping.support.dto.BlogContentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiBlogContentService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("M/d");

    private final WeeklyBlogContentService weeklyBlogContentService;
    private final BlogCardImageGenerator cardImageGenerator;
    private final ClaudeApiAdapter claudeApiAdapter;
    private final BlogPromptBuilder promptBuilder;

    public BlogContentResult generateAiBlogContent(int topN) {
        List<ScoredEntry> topEntries = weeklyBlogContentService.selectTopEntries(topN);
        if (topEntries.isEmpty()) {
            throw new IllegalStateException("[AI 블로그] 분석할 청약 데이터가 없습니다");
        }

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);

        String prompt = promptBuilder.build(topEntries, topN, weekStart, weekEnd);
        String aiText = claudeApiAdapter.generateBlogContent(prompt);

        String title = String.format("이번 주 주목할 청약 TOP %d (%s~%s)",
                topN, weekStart.format(DATE_FMT), weekEnd.format(DATE_FMT));

        List<BlogContentResult.BlogCardEntry> entries = new ArrayList<>();
        for (int i = 0; i < topEntries.size(); i++) {
            ScoredEntry entry = topEntries.get(i);
            int rank = i + 1;
            byte[] cardImage = cardImageGenerator.generateCardImage(entry.analysis(), entry.badge());

            entries.add(BlogContentResult.BlogCardEntry.createWithImage(
                    entry.subscription().getId(),
                    entry.subscription().getHouseName(),
                    rank,
                    entry.subscription().getHouseName(),
                    cardImage));
        }

        return BlogContentResult.builder()
                .generatedDate(today)
                .title(title)
                .blogText(aiText)
                .entries(entries)
                .build();
    }
}
