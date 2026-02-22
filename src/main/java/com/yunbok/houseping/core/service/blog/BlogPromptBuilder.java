package com.yunbok.houseping.core.service.blog;

import com.yunbok.houseping.core.domain.Subscription;
import com.yunbok.houseping.core.domain.SubscriptionPrice;
import com.yunbok.houseping.core.domain.SubscriptionStatus;
import com.yunbok.houseping.support.dto.PriceBadge;
import com.yunbok.houseping.support.dto.SubscriptionCardView;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
class BlogPromptBuilder {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("M/d");
    private static final DateTimeFormatter FULL_DATE_FMT = DateTimeFormatter.ofPattern("yyyy년 M월 d일");

    String build(List<SubscriptionCardView> allSubscriptions,
                 Map<Long, SubscriptionPrice> representativePrices,
                 int topN,
                 LocalDate weekStart, LocalDate weekEnd) {

        StringBuilder sb = new StringBuilder();

        // 역할 + 타겟 독자
        sb.append("당신은 부동산 청약 분석 전문가입니다.\n");
        sb.append("하우스핑(house-ping.com) 블로그에 게시할 주간 청약 분석을 작성해주세요.\n");
        sb.append("독자: 청약에 관심 있는 일반인. 친근하고 쉽게 \"안녕하세요, 여러분!\" 같은 톤으로.\n\n");

        // 기준 정보
        sb.append("[기준 정보]\n");
        sb.append("- 오늘 날짜: ").append(LocalDate.now().format(FULL_DATE_FMT)).append("\n");
        sb.append("- 분석 주간: ").append(weekStart.format(DATE_FMT)).append(" ~ ").append(weekEnd.format(DATE_FMT)).append("\n");
        sb.append("- 반드시 오늘 날짜 기준으로 분석하세요. 이미 개통/완공된 인프라를 '예정'이라고 쓰지 마세요.\n\n");

        // 데이터
        sb.append("[이번 주 전체 청약 리스트] (총 ").append(allSubscriptions.size()).append("건)\n\n");

        for (int i = 0; i < allSubscriptions.size(); i++) {
            SubscriptionCardView card = allSubscriptions.get(i);
            Subscription s = card.getSubscription();

            sb.append(i + 1).append(". ").append(s.getHouseName());
            if (s.getArea() != null) {
                sb.append(" (").append(s.getArea()).append(")");
            }
            sb.append("\n");

            if (s.getAddress() != null) {
                sb.append("   - 주소: ").append(s.getAddress()).append("\n");
            }
            if (s.getTotalSupplyCount() != null) {
                sb.append("   - 세대수: ").append(String.format("%,d", s.getTotalSupplyCount())).append("세대\n");
            }
            if (s.getHouseType() != null) {
                sb.append("   - 주택유형: ").append(s.getHouseType()).append("\n");
            }

            sb.append("   - 상태: ").append(s.getStatus() == SubscriptionStatus.ACTIVE ? "접수중" : "접수예정");
            if (s.getReceiptStartDate() != null) {
                sb.append(" (").append(s.getReceiptStartDate().format(DATE_FMT));
                if (s.getReceiptEndDate() != null) {
                    sb.append("~").append(s.getReceiptEndDate().format(DATE_FMT));
                }
                sb.append(")");
            }
            sb.append("\n");

            // 분양가 정보
            SubscriptionPrice price = representativePrices.get(s.getId());
            if (price != null) {
                sb.append("   - 대표 분양가: ").append(price.getTopAmountFormatted());
                if (price.getHouseType() != null) {
                    sb.append(" (").append(price.getHouseType()).append(" 기준)");
                }
                sb.append("\n");
            }

            sb.append("   - 가격 판정: ").append(badgeLabel(card.getPriceBadge())).append("\n");
            sb.append("\n");
        }

        // 요청
        sb.append("[요청]\n");
        sb.append("1. 위 리스트에서 가장 주목할 만한 TOP ").append(topN).append("개를 선정하세요.\n");
        sb.append("2. 선정 기준: 입지 가치, 가격 메리트(시세 대비 저렴), 대단지 여부, 접수 임박도, 시장 관심도 등을 종합 고려.\n");
        sb.append("3. 각 청약에 대해 입지, 가격, 장단점, 청약 전략을 분석하세요.\n");
        sb.append("4. 왜 이 청약을 선정했는지 근거를 간결하게 밝히세요.\n\n");

        // 작성 가이드
        sb.append("[작성 가이드]\n");
        sb.append("- 분량: 2000~2500자\n");
        sb.append("- 구조: 이번 주 시장 동향 요약 → TOP ").append(topN).append(" 각각 분석 → 종합 의견\n");
        sb.append("- 톤: 정보성 + 친근한 전문가, \"~입니다\" 체\n");
        sb.append("- 마크다운(##, **굵게** 등) 사용 가능\n");
        sb.append("- 이모지는 꼭 필요한 경우에만 사용\n");
        sb.append("- 제목은 포함하지 마세요 (별도 생성됨)\n");
        sb.append("- 글 말미에 투자 판단은 본인 책임이라는 면책 문구 포함\n\n");

        // 금지 사항
        sb.append("[주의사항]\n");
        sb.append("- 날짜 판단을 정확히 하세요. 오늘은 ").append(LocalDate.now().format(FULL_DATE_FMT)).append("입니다.\n");
        sb.append("  - 접수 시작일이 오늘 이후 → \"접수 예정\" (아직 시작 안 됨)\n");
        sb.append("  - 접수 시작일 ≤ 오늘 ≤ 접수 종료일 → \"접수중\"\n");
        sb.append("  - 접수 종료일이 오늘 이전 → \"접수 완료\"\n");
        sb.append("  - 아직 오지 않은 날짜를 '이미 지났다', '접수 완료' 등 과거형으로 쓰지 마세요.\n");
        sb.append("- 각 청약의 '상태' 필드를 그대로 따르세요. 직접 날짜를 재해석하지 마세요.\n");
        sb.append("- 제공된 데이터에 없는 구체적 수치(시세, 경쟁률, 평당가 등)를 지어내지 마세요.\n");
        sb.append("- 분양가는 위 데이터에 있는 경우에만 언급하세요.\n");
        sb.append("- 확인되지 않은 개발 호재, 교통 계획 등을 사실처럼 쓰지 마세요.\n");
        sb.append("- '가격 판정: 판단 불가'인 청약은 가격 분석 없이 입지·규모 위주로 서술하세요.\n");

        return sb.toString();
    }

    private String badgeLabel(PriceBadge badge) {
        return switch (badge) {
            case CHEAP -> "저렴 (시세 대비 저렴)";
            case EXPENSIVE -> "비쌈 (시세 대비 높음)";
            case UNKNOWN -> "판단 불가 (비교 데이터 부족)";
        };
    }
}
