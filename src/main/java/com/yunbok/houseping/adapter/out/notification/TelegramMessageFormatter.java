package com.yunbok.houseping.adapter.out.notification;

import com.yunbok.houseping.domain.model.DailyNotificationReport;
import com.yunbok.houseping.domain.model.NotificationTarget;
import com.yunbok.houseping.domain.model.SubscriptionInfo;
import com.yunbok.houseping.domain.port.out.SubscriptionMessageFormatter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Telegram용 메시지 포맷터
 * Telegram HTML 형식에 맞게 메시지를 포맷팅
 */
@Component
public class TelegramMessageFormatter implements SubscriptionMessageFormatter {

    @Override
    public String formatBatchSummary(List<SubscriptionInfo> subscriptions) {
        if (subscriptions.isEmpty()) {
            return formatNoDataMessage();
        }

        StringBuilder summary = new StringBuilder();
        summary.append("오늘의 신규 청약 정보 ")
               .append(subscriptions.size())
               .append("개\n\n");

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
        return "청약 알리미 오류 발생\n\n" + errorMessage;
    }

    @Override
    public String formatNoDataMessage() {
        return "📭 오늘은 신규 청약 정보가 없습니다.";
    }

    @Override
    public String formatDailyReport(DailyNotificationReport report) {
        if (report.isEmpty()) {
            return "📭 오늘은 알림이 없습니다.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("📋 <b>오늘의 청약 알림</b>\n\n");

        // 접수 마감 (오늘) - 긴급
        if (!report.receiptEndToday().isEmpty()) {
            sb.append("⚠️ <b>접수 마감 (오늘)</b> ─────────\n");
            for (NotificationTarget target : report.receiptEndToday()) {
                sb.append(formatReceiptEndItem(target));
            }
            sb.append("\n");
        }

        // 접수 시작 (내일) - 중요
        if (!report.receiptStartTomorrow().isEmpty()) {
            sb.append("📅 <b>접수 시작 (내일)</b> ─────────\n");
            for (NotificationTarget target : report.receiptStartTomorrow()) {
                sb.append(formatReceiptStartItem(target));
            }
            sb.append("\n");
        }

        // 신규 청약 - 정보
        if (!report.newSubscriptions().isEmpty()) {
            sb.append("🆕 <b>신규 청약</b> ─────────\n");
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
        sb.append("• <b>").append(escapeHtml(target.houseName())).append("</b>")
          .append(" | ").append(target.area() != null ? escapeHtml(target.area()) : "-")
          .append(" | ").append(formatSupplyCount(target.totalSupplyCount())).append("\n");
        sb.append("  접수 마감: 오늘까지");
        if (target.detailUrl() != null) {
            sb.append(" | <a href=\"").append(target.detailUrl()).append("\">상세보기</a>");
        }
        sb.append("\n");
        return sb.toString();
    }

    private String formatReceiptStartItem(NotificationTarget target) {
        StringBuilder sb = new StringBuilder();
        sb.append("• <b>").append(escapeHtml(target.houseName())).append("</b>")
          .append(" | ").append(target.area() != null ? escapeHtml(target.area()) : "-")
          .append(" | ").append(formatSupplyCount(target.totalSupplyCount())).append("\n");
        sb.append("  접수 기간: ").append(formatDate(target.receiptStartDate()))
          .append(" ~ ").append(formatDate(target.receiptEndDate()));
        if (target.detailUrl() != null) {
            sb.append(" | <a href=\"").append(target.detailUrl()).append("\">상세보기</a>");
        }
        sb.append("\n");
        return sb.toString();
    }

    private String formatNewSubscriptionItem(SubscriptionInfo subscription) {
        StringBuilder sb = new StringBuilder();
        sb.append("• <b>").append(escapeHtml(subscription.getHouseName())).append("</b>")
          .append(" | ").append(escapeHtml(subscription.getArea()))
          .append(" | ").append(formatSupplyCount(subscription.getTotalSupplyCount())).append("\n");
        sb.append("  접수 기간: ").append(formatDate(subscription.getReceiptStartDate()))
          .append(" ~ ").append(formatDate(subscription.getReceiptEndDate()));
        if (subscription.getDetailUrl() != null) {
            sb.append(" | <a href=\"").append(subscription.getDetailUrl()).append("\">상세보기</a>");
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

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;");
    }
}
