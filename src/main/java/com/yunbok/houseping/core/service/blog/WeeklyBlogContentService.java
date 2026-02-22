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

    /** 시세 대비 저렴한 청약에 부여하는 가산점 */
    private static final int SCORE_CHEAP_BADGE = 100;
    /** 예상 차익 1,000만 원당 가산점 (최대 50) */
    private static final int SCORE_PROFIT_PER_UNIT = 1000;
    private static final int SCORE_PROFIT_MAX = 50;
    /** 현재 접수 중인 청약 가산점 */
    private static final int SCORE_ACTIVE_STATUS = 20;
    /** 500세대 이상 대단지 가산점 */
    private static final int SCORE_LARGE_SUPPLY = 10;
    private static final int LARGE_SUPPLY_THRESHOLD = 500;
    /** 접수 시작까지 7일 이내인 경우 가산점 */
    private static final int SCORE_UPCOMING_RECEIPT = 15;
    private static final int UPCOMING_RECEIPT_DAYS = 7;

    private final SubscriptionSearchService searchService;
    private final SubscriptionAnalysisService analysisService;
    private final PriceBadgeCalculator priceBadgeCalculator;
    private final BlogCardImageGenerator cardImageGenerator;
    private final BlogNarrativeBuilder narrativeBuilder;

    public BlogContentResult generateWeeklyContent(int topN) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);

        List<ScoredEntry> top = selectTopEntries(topN);

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
            String narrative = narrativeBuilder.build(entry, rank);

            blogText.append("\n---\n\n");
            blogText.append(narrative);

            entries.add(BlogContentResult.BlogCardEntry.createWithImage(
                    entry.subscription().getId(),
                    entry.subscription().getHouseName(),
                    rank,
                    narrative,
                    cardImage));
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
        List<SubscriptionCardView> allCards = searchService.getAllActiveAndUpcoming();

        List<ScoredEntry> scored = new ArrayList<>();
        for (SubscriptionCardView card : allCards) {
            Subscription subscription = card.getSubscription();
            try {
                SubscriptionAnalysisResult analysis = analysisService.analyze(subscription.getId());
                PriceBadge badge = priceBadgeCalculator.computePriceBadge(subscription);
                int score = computeScore(subscription, badge, analysis);
                scored.add(new ScoredEntry(subscription, analysis, badge, score));
            } catch (Exception e) {
                log.warn("분석 실패: {} ({})", subscription.getHouseName(), subscription.getId(), e);
            }
        }

        scored.sort(Comparator.comparingInt(ScoredEntry::score).reversed());
        return scored.stream().limit(topN).toList();
    }

    private int computeScore(Subscription subscription, PriceBadge badge, SubscriptionAnalysisResult analysis) {
        int score = 0;

        if (badge == PriceBadge.CHEAP) {
            score += SCORE_CHEAP_BADGE;
        }

        HouseTypeComparison rep = analysis.getRepresentativeComparison();
        if (rep != null && rep.getEstimatedProfit() != null && rep.getEstimatedProfit() > 0) {
            score += (int) Math.min(rep.getEstimatedProfit() / SCORE_PROFIT_PER_UNIT, SCORE_PROFIT_MAX);
        }

        if (subscription.getStatus() == SubscriptionStatus.ACTIVE) {
            score += SCORE_ACTIVE_STATUS;
        }

        if (subscription.getTotalSupplyCount() != null && subscription.getTotalSupplyCount() >= LARGE_SUPPLY_THRESHOLD) {
            score += SCORE_LARGE_SUPPLY;
        }

        if (subscription.getReceiptStartDate() != null) {
            long daysUntilStart = ChronoUnit.DAYS.between(LocalDate.now(), subscription.getReceiptStartDate());
            if (daysUntilStart >= 0 && daysUntilStart <= UPCOMING_RECEIPT_DAYS) {
                score += SCORE_UPCOMING_RECEIPT;
            }
        }

        return score;
    }

    record ScoredEntry(Subscription subscription, SubscriptionAnalysisResult analysis, PriceBadge badge, int score) {}
}
