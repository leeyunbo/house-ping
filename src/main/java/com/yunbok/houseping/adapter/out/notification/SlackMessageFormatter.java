package com.yunbok.houseping.adapter.out.notification;

import com.yunbok.houseping.domain.model.DailyNotificationReport;
import com.yunbok.houseping.domain.model.NotificationTarget;
import com.yunbok.houseping.domain.model.SubscriptionInfo;
import com.yunbok.houseping.domain.port.out.SubscriptionMessageFormatter;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Slack용 메시지 포맷터
 * Slack mrkdwn 형식에 맞게 메시지를 포맷팅
 */
@Component
@Primary
public class SlackMessageFormatter implements SubscriptionMessageFormatter {

    @Override
    public String formatBatchSummary(List<SubscriptionInfo> subscriptions) {
        if (subscriptions.isEmpty()) {
            return formatNoDataMessage();
        }

        StringBuilder summary = new StringBuilder();
        summary.append(":tada: *오늘의 신규 청약 정보 ")
               .append(subscriptions.size())
               .append("개*\n\n");

        for (int i = 0; i < subscriptions.size(); i++) {
            SubscriptionInfo sub = subscriptions.get(i);
            summary.append(i + 1)
                   .append(". ")
                   .append(sub.getSimpleDisplayMessage());
        }

        return summary.toString();
    }

    @Override
    public String formatSubscription(SubscriptionInfo subscription) {
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
        if (report.isEmpty()) {
            return ":mailbox_with_no_mail: 오늘은 알림이 없습니다.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(":clipboard: *오늘의 청약 알림*\n\n");

        // 접수 마감 (오늘) - 긴급
        if (!report.receiptEndToday().isEmpty()) {
            sb.append(":warning: *접수 마감 (오늘)* ─────────\n");
            for (NotificationTarget target : report.receiptEndToday()) {
                sb.append(formatReceiptEndItem(target));
            }
            sb.append("\n");
        }

        // 접수 시작 (내일) - 중요
        if (!report.receiptStartTomorrow().isEmpty()) {
            sb.append(":calendar: *접수 시작 (내일)* ─────────\n");
            for (NotificationTarget target : report.receiptStartTomorrow()) {
                sb.append(formatReceiptStartItem(target));
            }
            sb.append("\n");
        }

        // 신규 청약 - 정보
        if (!report.newSubscriptions().isEmpty()) {
            sb.append(":new: *신규 청약* ─────────\n");
            for (SubscriptionInfo subscription : report.newSubscriptions()) {
                sb.append(formatNewSubscriptionItem(subscription));
            }
            sb.append("\n");
        }

        // 요약
        sb.append("━━━━━━━━━━━━━━━━━━━━\n");
        sb.append(String.format("신규 %d건 | 내일 접수 %d건 | 오늘 마감 %d건",
                report.newSubscriptions().size(),
                report.receiptStartTomorrow().size(),
                report.receiptEndToday().size()));

        return sb.toString();
    }

    private String formatReceiptEndItem(NotificationTarget target) {
        StringBuilder sb = new StringBuilder();
        sb.append("• *").append(target.houseName()).append("*")
          .append(" | ").append(target.area() != null ? target.area() : "-")
          .append(" | ").append(formatSupplyCount(target.totalSupplyCount())).append("\n");
        sb.append("  접수 마감: 오늘까지");
        if (target.detailUrl() != null) {
            sb.append(" | <").append(target.detailUrl()).append("|상세보기>");
        }
        sb.append("\n");
        return sb.toString();
    }

    private String formatReceiptStartItem(NotificationTarget target) {
        StringBuilder sb = new StringBuilder();
        sb.append("• *").append(target.houseName()).append("*")
          .append(" | ").append(target.area() != null ? target.area() : "-")
          .append(" | ").append(formatSupplyCount(target.totalSupplyCount())).append("\n");
        sb.append("  접수 기간: ").append(formatDate(target.receiptStartDate()))
          .append(" ~ ").append(formatDate(target.receiptEndDate()));
        if (target.detailUrl() != null) {
            sb.append(" | <").append(target.detailUrl()).append("|상세보기>");
        }
        sb.append("\n");
        return sb.toString();
    }

    private String formatNewSubscriptionItem(SubscriptionInfo subscription) {
        StringBuilder sb = new StringBuilder();
        sb.append("• *").append(subscription.getHouseName()).append("*")
          .append(" | ").append(subscription.getArea())
          .append(" | ").append(formatSupplyCount(subscription.getTotalSupplyCount())).append("\n");
        sb.append("  접수 기간: ").append(formatDate(subscription.getReceiptStartDate()))
          .append(" ~ ").append(formatDate(subscription.getReceiptEndDate()));
        if (subscription.getDetailUrl() != null) {
            sb.append(" | <").append(subscription.getDetailUrl()).append("|상세보기>");
        }
        sb.append("\n");
        return sb.toString();
    }

    private String formatSupplyCount(Integer count) {
        return count != null ? count + "세대" : "-";
    }

    private String formatDate(LocalDate date) {
        if (date == null) {
            return "-";
        }
        return date.format(DateTimeFormatter.ofPattern("M/d"));
    }
}
