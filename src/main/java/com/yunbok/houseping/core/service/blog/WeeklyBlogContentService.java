package com.yunbok.houseping.core.service.blog;

import com.yunbok.houseping.core.domain.Subscription;
import com.yunbok.houseping.core.domain.SubscriptionStatus;
import com.yunbok.houseping.core.service.subscription.PriceBadgeCalculator;
import com.yunbok.houseping.core.service.subscription.SubscriptionAnalysisService;
import com.yunbok.houseping.core.service.subscription.SubscriptionSearchService;
import com.yunbok.houseping.support.dto.BlogContentResult;
import com.yunbok.houseping.support.dto.HouseTypeComparison;
import com.yunbok.houseping.support.dto.PriceBadge;
import com.yunbok.houseping.support.dto.SubscriptionAnalysisResult;
import com.yunbok.houseping.support.dto.SubscriptionCardView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyBlogContentService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("M/d");
    private static final DateTimeFormatter DATE_FMT_FULL = DateTimeFormatter.ofPattern("M월 d일");

    private final SubscriptionSearchService searchService;
    private final SubscriptionAnalysisService analysisService;
    private final PriceBadgeCalculator priceBadgeCalculator;
    private final BlogCardImageGenerator cardImageGenerator;

    public BlogContentResult generateWeeklyContent(int topN) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);

        List<ScoredEntry> top = selectTopEntries(topN);

        // 카드 이미지 생성 및 텍스트 빌드
        List<BlogContentResult.BlogCardEntry> entries = new ArrayList<>();
        StringBuilder blogText = new StringBuilder();

        String title = String.format("이번 주 주목할 청약 TOP %d (%s~%s)",
                topN, weekStart.format(DATE_FMT), weekEnd.format(DATE_FMT));

        blogText.append(title).append("\n\n");
        blogText.append("안녕하세요, 하우스핑입니다.\n");
        blogText.append("이번 주 서울/경기 지역에서 눈여겨볼 청약을 정리해 드립니다.\n");

        for (int i = 0; i < top.size(); i++) {
            ScoredEntry entry = top.get(i);
            int rank = i + 1;

            byte[] cardImage = cardImageGenerator.generateCardImage(entry.analysis(), entry.badge());
            String narrative = buildNarrative(entry, rank);

            blogText.append("\n---\n\n");
            blogText.append(narrative);

            entries.add(BlogContentResult.BlogCardEntry.builder()
                    .subscriptionId(entry.sub().getId())
                    .houseName(entry.sub().getHouseName())
                    .rank(rank)
                    .narrativeText(narrative)
                    .cardImage(cardImage)
                    .build());
        }

        blogText.append("\n---\n\n");
        blogText.append("더 자세한 분석은 house-ping.com 에서 확인하세요.\n\n");
        blogText.append("※ 본 콘텐츠는 청약Home·LH·국토교통부 공공데이터를 기반으로 작성되었으며, 정보의 정확성을 보장하지 않습니다. ");
        blogText.append("시세 및 예상 차익은 주변 실거래가를 참고한 추정치이며, 실제와 다를 수 있습니다. ");
        blogText.append("투자 및 청약 판단의 책임은 본인에게 있습니다.\n\n");
        blogText.append("#청약 #분양 #서울청약 #경기청약 #청약분석");

        return BlogContentResult.builder()
                .generatedDate(today)
                .title(title)
                .blogText(blogText.toString())
                .entries(entries)
                .build();
    }

    List<ScoredEntry> selectTopEntries(int topN) {
        var homeData = searchService.getHomeData(null);
        List<SubscriptionCardView> allCards = new ArrayList<>();
        allCards.addAll(homeData.getActiveSubscriptions());
        allCards.addAll(homeData.getUpcomingSubscriptions());

        List<ScoredEntry> scored = new ArrayList<>();
        for (SubscriptionCardView card : allCards) {
            Subscription sub = card.getSubscription();
            try {
                SubscriptionAnalysisResult analysis = analysisService.analyze(sub.getId());
                PriceBadge badge = priceBadgeCalculator.computePriceBadge(sub);
                int score = computeScore(sub, badge, analysis);
                scored.add(new ScoredEntry(sub, analysis, badge, score));
            } catch (Exception e) {
                log.warn("분석 실패: {} ({})", sub.getHouseName(), sub.getId(), e);
            }
        }

        scored.sort(Comparator.comparingInt(ScoredEntry::score).reversed());
        return scored.stream().limit(topN).toList();
    }

    private int computeScore(Subscription sub, PriceBadge badge, SubscriptionAnalysisResult analysis) {
        int score = 0;

        if (badge == PriceBadge.CHEAP) {
            score += 100;
        }

        HouseTypeComparison rep = analysis.getRepresentativeComparison();
        if (rep != null && rep.getEstimatedProfit() != null && rep.getEstimatedProfit() > 0) {
            score += Math.min(rep.getEstimatedProfit() / 1000, 50);
        }

        if (sub.getStatus() == SubscriptionStatus.ACTIVE) {
            score += 20;
        }

        if (sub.getTotalSupplyCount() != null && sub.getTotalSupplyCount() >= 500) {
            score += 10;
        }

        if (sub.getReceiptStartDate() != null) {
            long daysUntilStart = ChronoUnit.DAYS.between(LocalDate.now(), sub.getReceiptStartDate());
            if (daysUntilStart >= 0 && daysUntilStart <= 7) {
                score += 15;
            }
        }

        return score;
    }

    private String buildNarrative(ScoredEntry entry, int rank) {
        Subscription sub = entry.sub();
        HouseTypeComparison rep = entry.analysis().getRepresentativeComparison();
        StringBuilder sb = new StringBuilder();

        sb.append(rank).append(". ").append(sub.getHouseName());
        if (sub.getArea() != null) {
            sb.append(" (").append(sub.getArea()).append(")");
        }
        sb.append("\n[카드 이미지]\n\n");

        // 위치/규모
        if (sub.getArea() != null) {
            sb.append(sub.getArea()).append("에 위치한 ").append(sub.getHouseName()).append("는 ");
        } else {
            sb.append(sub.getHouseName()).append("는 ");
        }
        if (sub.getTotalSupplyCount() != null) {
            sb.append("총 ").append(String.format("%,d", sub.getTotalSupplyCount())).append("세대 규모로,\n");
        }

        // 접수 상태
        if (sub.getStatus() == SubscriptionStatus.ACTIVE) {
            sb.append("현재 접수 중입니다");
        } else {
            sb.append("접수 예정입니다");
        }
        if (sub.getReceiptStartDate() != null) {
            sb.append(" (").append(sub.getReceiptStartDate().format(DATE_FMT));
            if (sub.getReceiptEndDate() != null) {
                sb.append("~").append(sub.getReceiptEndDate().format(DATE_FMT));
            }
            sb.append(")");
        }
        sb.append(".\n\n");

        // 가격 정보
        if (rep != null && rep.getSupplyPrice() != null) {
            sb.append(rep.getHouseType() != null ? rep.getHouseType() + " 기준 " : "");
            sb.append("분양가는 약 ").append(formatPriceKorean(rep.getSupplyPrice())).append("으로,\n");

            if (rep.hasMarketData()) {
                sb.append("주변 신축 시세(").append(formatPriceKorean(rep.getMarketPrice())).append(") 대비 ");
                if (rep.getEstimatedProfit() != null) {
                    long profit = rep.getEstimatedProfit();
                    if (profit > 0) {
                        sb.append("약 ").append(formatPriceKorean(profit)).append("의 차익이 예상됩니다.\n");
                    } else if (profit < 0) {
                        sb.append("약 ").append(formatPriceKorean(Math.abs(profit))).append(" 높은 수준입니다.\n");
                    } else {
                        sb.append("비슷한 수준입니다.\n");
                    }
                }
            }
        }

        return sb.toString();
    }

    private String formatPriceKorean(long amount) {
        if (amount >= 10000) {
            long uk = amount / 10000;
            long rest = amount % 10000;
            if (rest == 0) {
                return uk + "억 원";
            }
            return uk + "억 " + String.format("%,d", rest) + "만 원";
        }
        return String.format("%,d", amount) + "만 원";
    }

    record ScoredEntry(Subscription sub, SubscriptionAnalysisResult analysis, PriceBadge badge, int score) {}
}
