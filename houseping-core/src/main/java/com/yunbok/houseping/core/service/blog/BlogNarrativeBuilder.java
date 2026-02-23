package com.yunbok.houseping.core.service.blog;

import com.yunbok.houseping.core.domain.Subscription;
import com.yunbok.houseping.core.domain.SubscriptionStatus;
import com.yunbok.houseping.core.service.blog.WeeklyBlogContentService.ScoredEntry;
import com.yunbok.houseping.support.dto.HouseTypeComparison;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
class BlogNarrativeBuilder {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("M/d");

    String build(ScoredEntry entry, int rank) {
        Subscription subscription = entry.subscription();
        HouseTypeComparison rep = entry.analysis().getRepresentativeComparison();
        StringBuilder sb = new StringBuilder();

        sb.append(rank).append(". ").append(subscription.getHouseName());
        if (subscription.getArea() != null) {
            sb.append(" (").append(subscription.getArea()).append(")");
        }
        sb.append("\n[카드 이미지]\n\n");

        // 위치/규모
        if (subscription.getArea() != null) {
            sb.append(subscription.getArea()).append("에 위치한 ").append(subscription.getHouseName()).append("는 ");
        } else {
            sb.append(subscription.getHouseName()).append("는 ");
        }
        if (subscription.getTotalSupplyCount() != null) {
            sb.append("총 ").append(String.format("%,d", subscription.getTotalSupplyCount())).append("세대 규모로,\n");
        }

        // 접수 상태
        if (subscription.getStatus() == SubscriptionStatus.ACTIVE) {
            sb.append("현재 접수 중입니다");
        } else {
            sb.append("접수 예정입니다");
        }
        if (subscription.getReceiptStartDate() != null) {
            sb.append(" (").append(subscription.getReceiptStartDate().format(DATE_FMT));
            if (subscription.getReceiptEndDate() != null) {
                sb.append("~").append(subscription.getReceiptEndDate().format(DATE_FMT));
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
}
