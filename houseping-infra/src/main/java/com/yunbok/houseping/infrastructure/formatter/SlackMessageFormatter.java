package com.yunbok.houseping.infrastructure.formatter;

import com.yunbok.houseping.core.domain.Subscription;
import com.yunbok.houseping.support.dto.DailyNotificationReport;
import com.yunbok.houseping.support.dto.NotificationTarget;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Slack용 메시지 포맷터
 * Slack mrkdwn 형식에 맞게 메시지를 포맷팅
 */
@Component
@Primary
public class SlackMessageFormatter implements SubscriptionMessageFormatter {

    @Override
    public String formatBatchSummary(List<Subscription> subscriptions) {
        if (subscriptions.isEmpty()) {
            return formatNoDataMessage();
        }

        StringBuilder summary = new StringBuilder();
        summary.append(":tada: *오늘의 신규 청약 정보 ")
               .append(subscriptions.size())
               .append("개*\n\n");

        for (int i = 0; i < subscriptions.size(); i++) {
            Subscription sub = subscriptions.get(i);
            summary.append(i + 1)
                   .append(". ")
                   .append(sub.getSimpleDisplayMessage());
        }

        return summary.toString();
    }

    @Override
    public String formatSubscription(Subscription subscription) {
        return subscription.getDisplayMessage();
    }

    @Override
    public String formatErrorMessage(String errorMessage) {
        return ":rotating_light: *청약 알리미 오류 발생*\n\n" + errorMessage;
    }

    @Override
    public String formatNoDataMessage() {
        return ":mailbox_with_no_mail: 오늘은 신규 청약 정보가 없습니다.";
    }

    @Override
    public String formatDailyReport(DailyNotificationReport report) {
        StringBuilder sb = new StringBuilder();

        // 헤더 - 오늘 날짜
        sb.append(":bell: *").append(formatTodayHeader()).append(" 청약 알림*\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━\n\n");

        // 섹션 1: 내 관심 청약 (등록한 알림)
        sb.append(":pushpin: *내 관심 청약*\n");
        boolean hasMySubscriptions = !report.receiptEndToday().isEmpty() || !report.receiptStartTomorrow().isEmpty();

        if (!hasMySubscriptions) {
            sb.append("   등록된 알림 없음\n");
        } else {
            // 오늘 마감 - 긴급
            if (!report.receiptEndToday().isEmpty()) {
                sb.append("\n:red_circle: *오늘 마감*\n");
                for (NotificationTarget target : report.receiptEndToday()) {
                    sb.append(formatCompactItem(target.houseName(), target.area(), target.totalSupplyCount(), target.detailUrl()));
                }
            }

            // 내일 접수 시작
            if (!report.receiptStartTomorrow().isEmpty()) {
                sb.append("\n:large_green_circle: *내일 접수 시작*\n");
                for (NotificationTarget target : report.receiptStartTomorrow()) {
                    sb.append(formatCompactItem(target.houseName(), target.area(), target.totalSupplyCount(), target.detailUrl()));
                }
            }
        }

        sb.append("\n━━━━━━━━━━━━━━━━━━━━\n\n");

        // 섹션 2: 오늘의 청약 현황 (신규)
        sb.append(":newspaper: *오늘의 신규 청약*\n");
        if (report.newSubscriptions().isEmpty()) {
            sb.append("   신규 청약 없음\n");
        } else {
            sb.append("\n");
            int displayCount = Math.min(report.newSubscriptions().size(), 5);
            for (int i = 0; i < displayCount; i++) {
                Subscription sub = report.newSubscriptions().get(i);
                sb.append(formatCompactItem(sub.getHouseName(), sub.getArea(), sub.getTotalSupplyCount(), sub.getDetailUrl()));
            }
            if (report.newSubscriptions().size() > 5) {
                sb.append("   _외 ").append(report.newSubscriptions().size() - 5).append("건..._\n");
            }
        }

        // 요약
        sb.append("\n━━━━━━━━━━━━━━━━━━━━\n");
        sb.append(String.format(":bar_chart: 오늘 마감 *%d*건 | 내일 접수 *%d*건 | 신규 *%d*건",
                report.receiptEndToday().size(),
                report.receiptStartTomorrow().size(),
                report.newSubscriptions().size()));

        return sb.toString();
    }

    public String formatSchedulerError(String schedulerName, String timestamp, String errorMessage, String stackTrace) {
        return String.format(
                ":rotating_light: *스케줄러 실행 실패*\n\n"
                        + "*스케줄러:* %s\n"
                        + "*시각:* %s\n"
                        + "*에러:* %s\n"
                        + "```%s```",
                schedulerName, timestamp, errorMessage, stackTrace
        );
    }

    public String formatSchedulerErrorFallback(String schedulerName, String timestamp, String errorMessage) {
        return String.format("[%s] %s 실행 실패: %s", timestamp, schedulerName, errorMessage);
    }

    private String formatTodayHeader() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M월 d일(E)", Locale.KOREAN);
        return today.format(formatter);
    }

    private String formatCompactItem(String houseName, String area, Integer supplyCount, String detailUrl) {
        StringBuilder sb = new StringBuilder();
        sb.append("   • ").append(houseName);
        sb.append(" (").append(area != null ? area : "-");
        sb.append(", ").append(formatSupplyCount(supplyCount)).append(")");
        if (detailUrl != null) {
            sb.append(" <").append(detailUrl).append("|보기>");
        }
        sb.append("\n");
        return sb.toString();
    }

    private String formatSupplyCount(Integer count) {
        return count != null ? count + "세대" : "-";
    }
}
