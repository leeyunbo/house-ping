package com.yunbok.houseping.core.service.blog;

import com.yunbok.houseping.core.domain.Subscription;
import com.yunbok.houseping.core.domain.SubscriptionStatus;
import com.yunbok.houseping.core.service.blog.WeeklyBlogContentService.ScoredEntry;
import com.yunbok.houseping.support.dto.CompetitionRateDetailRow;
import com.yunbok.houseping.support.dto.HouseTypeComparison;
import com.yunbok.houseping.support.dto.PriceBadge;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
class BlogPromptBuilder {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("M/d");

    String build(List<ScoredEntry> entries, int topN, LocalDate weekStart, LocalDate weekEnd) {
        StringBuilder sb = new StringBuilder();
        sb.append("당신은 부동산 청약 분석 전문가입니다. ");
        sb.append("하우스핑(house-ping.com) 블로그에 게시할 주간 청약 분석을 작성해주세요.\n\n");

        sb.append("[데이터]\n");
        sb.append("이번 주: ").append(weekStart.format(DATE_FMT)).append(" ~ ").append(weekEnd.format(DATE_FMT)).append("\n");
        sb.append("TOP ").append(topN).append(" 청약:\n\n");

        for (int i = 0; i < entries.size(); i++) {
            ScoredEntry entry = entries.get(i);
            Subscription subscription = entry.subscription();
            HouseTypeComparison rep = entry.analysis().getRepresentativeComparison();

            sb.append(i + 1).append(". ").append(subscription.getHouseName());
            if (subscription.getArea() != null) {
                sb.append(" (").append(subscription.getArea()).append(")");
            }
            sb.append("\n");

            if (subscription.getAddress() != null) {
                sb.append("   - 주소: ").append(subscription.getAddress()).append("\n");
            }
            if (subscription.getTotalSupplyCount() != null) {
                sb.append("   - 세대수: ").append(String.format("%,d", subscription.getTotalSupplyCount())).append("세대\n");
            }

            sb.append("   - 상태: ").append(subscription.getStatus() == SubscriptionStatus.ACTIVE ? "접수중" : "접수예정");
            if (subscription.getReceiptStartDate() != null) {
                sb.append(" (").append(subscription.getReceiptStartDate().format(DATE_FMT));
                if (subscription.getReceiptEndDate() != null) {
                    sb.append("~").append(subscription.getReceiptEndDate().format(DATE_FMT));
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
