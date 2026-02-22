package com.yunbok.houseping.core.service.blog;

import com.yunbok.houseping.adapter.api.ClaudeApiAdapter;
import com.yunbok.houseping.core.domain.Subscription;
import com.yunbok.houseping.core.domain.SubscriptionStatus;
import com.yunbok.houseping.core.service.blog.WeeklyBlogContentService.ScoredEntry;
import com.yunbok.houseping.support.dto.BlogContentResult;
import com.yunbok.houseping.support.dto.CompetitionRateDetailRow;
import com.yunbok.houseping.support.dto.HouseTypeComparison;
import com.yunbok.houseping.support.dto.PriceBadge;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiBlogContentService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("M/d");

    private final WeeklyBlogContentService weeklyBlogContentService;
    private final BlogCardImageGenerator cardImageGenerator;
    private final ClaudeApiAdapter claudeApiAdapter;

    public Optional<BlogContentResult> generateAiBlogContent(int topN) {
        List<ScoredEntry> topEntries = weeklyBlogContentService.selectTopEntries(topN);
        if (topEntries.isEmpty()) {
            log.warn("[AI 블로그] 분석할 청약 데이터가 없습니다");
            return Optional.empty();
        }

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);

        String prompt = buildPrompt(topEntries, topN, weekStart, weekEnd);
        Optional<String> aiText = claudeApiAdapter.generateBlogContent(prompt);

        if (aiText.isEmpty()) {
            log.warn("[AI 블로그] Claude API 응답이 비어있습니다");
            return Optional.empty();
        }

        String title = String.format("이번 주 주목할 청약 TOP %d (%s~%s)",
                topN, weekStart.format(DATE_FMT), weekEnd.format(DATE_FMT));

        List<BlogContentResult.BlogCardEntry> entries = new ArrayList<>();
        for (int i = 0; i < topEntries.size(); i++) {
            ScoredEntry entry = topEntries.get(i);
            int rank = i + 1;
            byte[] cardImage = cardImageGenerator.generateCardImage(entry.analysis(), entry.badge());

            entries.add(BlogContentResult.BlogCardEntry.builder()
                    .subscriptionId(entry.sub().getId())
                    .houseName(entry.sub().getHouseName())
                    .rank(rank)
                    .narrativeText(entry.sub().getHouseName())
                    .cardImage(cardImage)
                    .build());
        }

        return Optional.of(BlogContentResult.builder()
                .generatedDate(today)
                .title(title)
                .blogText(aiText.get())
                .entries(entries)
                .build());
    }

    private String buildPrompt(List<ScoredEntry> entries, int topN,
                               LocalDate weekStart, LocalDate weekEnd) {
        StringBuilder sb = new StringBuilder();
        sb.append("당신은 부동산 청약 분석 전문가입니다. ");
        sb.append("하우스핑(house-ping.com) 블로그에 게시할 주간 청약 분석을 작성해주세요.\n\n");

        sb.append("[데이터]\n");
        sb.append("이번 주: ").append(weekStart.format(DATE_FMT)).append(" ~ ").append(weekEnd.format(DATE_FMT)).append("\n");
        sb.append("TOP ").append(topN).append(" 청약:\n\n");

        for (int i = 0; i < entries.size(); i++) {
            ScoredEntry entry = entries.get(i);
            Subscription sub = entry.sub();
            HouseTypeComparison rep = entry.analysis().getRepresentativeComparison();

            sb.append(i + 1).append(". ").append(sub.getHouseName());
            if (sub.getArea() != null) {
                sb.append(" (").append(sub.getArea()).append(")");
            }
            sb.append("\n");

            if (sub.getAddress() != null) {
                sb.append("   - 주소: ").append(sub.getAddress()).append("\n");
            }
            if (sub.getTotalSupplyCount() != null) {
                sb.append("   - 세대수: ").append(String.format("%,d", sub.getTotalSupplyCount())).append("세대\n");
            }

            sb.append("   - 상태: ").append(sub.getStatus() == SubscriptionStatus.ACTIVE ? "접수중" : "접수예정");
            if (sub.getReceiptStartDate() != null) {
                sb.append(" (").append(sub.getReceiptStartDate().format(DATE_FMT));
                if (sub.getReceiptEndDate() != null) {
                    sb.append("~").append(sub.getReceiptEndDate().format(DATE_FMT));
                }
                sb.append(")");
            }
            sb.append("\n");

            if (rep != null && rep.getSupplyPrice() != null) {
                sb.append("   - 분양가: ").append(rep.getSupplyPriceFormatted());
                if (rep.hasMarketData()) {
                    sb.append(" / 시세: ").append(rep.getMarketPriceFormatted());
                    sb.append(" / 예상 차익: ").append(rep.getEstimatedProfitFormatted());
                }
                sb.append("\n");
            }

            sb.append("   - 가격 배지: ").append(badgeLabel(entry.badge())).append("\n");

            if (entry.analysis().hasCompetitionRates()) {
                sb.append("   - 경쟁률: ");
                List<CompetitionRateDetailRow> rates = entry.analysis().getCompetitionRates();
                for (int j = 0; j < Math.min(rates.size(), 3); j++) {
                    CompetitionRateDetailRow rate = rates.get(j);
                    if (j > 0) sb.append(", ");
                    sb.append(rate.getHouseType()).append(" ").append(rate.getCompetitionRate()).append(":1");
                }
                sb.append("\n");
            }

            sb.append("\n");
        }

        sb.append("[작성 가이드]\n");
        sb.append("- 전체 분량: 2000자 내외\n");
        sb.append("- 구조: 시장 동향 요약 -> 각 청약별 분석 (입지, 가격, 장단점, 전략) -> 종합 의견\n");
        sb.append("- 톤: 정보성 + 친근한 전문가\n");
        sb.append("- \"~입니다\" 체\n");
        sb.append("- 면책: 투자 판단은 본인 책임 문구 포함\n");
        sb.append("- 마크다운 문법 사용하지 마세요. 순수 텍스트로 작성해주세요.\n");

        return sb.toString();
    }

    private String badgeLabel(PriceBadge badge) {
        return switch (badge) {
            case CHEAP -> "저렴 (시세 대비 저렴)";
            case EXPENSIVE -> "비쌈 (시세 대비 높음)";
            case UNKNOWN -> "판단 불가";
        };
    }
}
