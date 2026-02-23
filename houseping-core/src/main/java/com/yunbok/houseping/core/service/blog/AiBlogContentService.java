package com.yunbok.houseping.core.service.blog;

import com.yunbok.houseping.core.domain.Subscription;
import com.yunbok.houseping.core.domain.SubscriptionPrice;
import com.yunbok.houseping.core.service.subscription.PriceBadgeCalculator;
import com.yunbok.houseping.core.service.subscription.SubscriptionSearchService;
import com.yunbok.houseping.core.port.AiContentPort;
import com.yunbok.houseping.core.port.SubscriptionPricePersistencePort;
import com.yunbok.houseping.support.dto.BlogContentResult;
import com.yunbok.houseping.support.dto.SubscriptionCardView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiBlogContentService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("M/d");

    private final SubscriptionSearchService subscriptionSearchService;
    private final SubscriptionPricePersistencePort subscriptionPriceStore;
    private final PriceBadgeCalculator priceBadgeCalculator;
    private final AiContentPort claudeApiClient;
    private final BlogPromptBuilder promptBuilder;

    public BlogContentResult generateAiBlogContent(int topN) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);

        List<SubscriptionCardView> weekSubscriptions = subscriptionSearchService.getSubscriptionCardsForWeek(weekStart, weekEnd);
        if (weekSubscriptions.isEmpty()) {
            throw new IllegalStateException("[AI 블로그] 이번 주 분석할 청약 데이터가 없습니다");
        }

        Map<Long, SubscriptionPrice> representativePrices = buildRepresentativePrices(weekSubscriptions);

        log.info("[AI 블로그] 이번 주({}/{} ~ {}/{}) 청약 {}건 (분양가 {}건) → AI에게 TOP {} 선정 + 분석 요청",
                weekStart.getMonthValue(), weekStart.getDayOfMonth(),
                weekEnd.getMonthValue(), weekEnd.getDayOfMonth(),
                weekSubscriptions.size(), representativePrices.size(), topN);

        String prompt = promptBuilder.build(weekSubscriptions, representativePrices, topN, weekStart, weekEnd);
        String aiText = claudeApiClient.generateBlogContent(prompt);

        String title = String.format("이번 주 주목할 청약 TOP %d (%s~%s)",
                topN, weekStart.format(DATE_FMT), weekEnd.format(DATE_FMT));

        return BlogContentResult.builder()
                .generatedDate(today)
                .title(title)
                .blogText(aiText)
                .entries(List.of())
                .build();
    }

    private Map<Long, SubscriptionPrice> buildRepresentativePrices(List<SubscriptionCardView> subscriptions) {
        Map<Long, SubscriptionPrice> result = new HashMap<>();
        for (SubscriptionCardView card : subscriptions) {
            Subscription s = card.getSubscription();
            if (s.getHouseManageNo() == null) continue;

            List<SubscriptionPrice> prices = subscriptionPriceStore.findByHouseManageNo(s.getHouseManageNo());
            if (prices.isEmpty()) continue;

            SubscriptionPrice rep = priceBadgeCalculator.selectRepresentativePrice(prices);
            if (rep != null && rep.getTopAmount() != null) {
                result.put(s.getId(), rep);
            }
        }
        return result;
    }
}
