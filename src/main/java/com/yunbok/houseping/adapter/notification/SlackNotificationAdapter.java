package com.yunbok.houseping.adapter.notification;

import com.yunbok.houseping.adapter.formatter.SlackMessageFormatter;
import com.yunbok.houseping.support.dto.DailyNotificationReport;
import com.yunbok.houseping.adapter.dto.SubscriptionInfo;
import com.yunbok.houseping.core.port.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

/**
 * Slack 알림 어댑터
 * feature.notification.slack-enabled=true 일 때만 활성화
 */
@Slf4j
@Component
@ConditionalOnProperty(
    name = "feature.notification.slack-enabled",
    havingValue = "true",
    matchIfMissing = false
)
public class SlackNotificationAdapter implements NotificationSender {

    private final WebClient webClient;
    private final String webhookUrl;
    private final SlackMessageFormatter messageFormatter;

    public SlackNotificationAdapter(
            @Value("${slack.webhook.url}") String webhookUrl,
            SlackMessageFormatter messageFormatter) {
        this.webhookUrl = webhookUrl;
        this.webClient = WebClient.create();
        this.messageFormatter = messageFormatter;
    }

    public void sendNewSubscriptions(List<SubscriptionInfo> subscriptions) {
        if (subscriptions.isEmpty()) {
            return;
        }
        String summaryMessage = messageFormatter.formatBatchSummary(subscriptions);
        sendSlackMessage(summaryMessage);

        subscriptions.forEach(this::sendSubscription);
    }

    public void sendSubscription(SubscriptionInfo subscription) {
        String message = messageFormatter.formatSubscription(subscription);
        sendSlackMessage(message);
    }

    public void sendErrorNotification(String errorMessage) {
        String message = messageFormatter.formatErrorMessage(errorMessage);
        sendSlackMessage(message);
    }

    public void sendNotification(String message) {
        sendSlackMessage(message);
    }

    public void sendNoDataNotification() {
        sendSlackMessage(messageFormatter.formatNoDataMessage());
    }

    public void sendDailyReport(DailyNotificationReport report) {
        String message = messageFormatter.formatDailyReport(report);
        sendSlackMessage(message);
    }

    /**
     * Slack Webhook으로 메시지 발송
     */
    private void sendSlackMessage(String message) {
        try {
            log.info("[Slack] 메시지 발송 시도");

            Map<String, Object> request = Map.of(
                "blocks", List.of(
                    Map.of(
                        "type", "section",
                        "text", Map.of(
                            "type", "mrkdwn",
                            "text", message
                        )
                    )
                ),
                "text", message  // fallback용
            );

            webClient.post()
                    .uri(webhookUrl)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("[Slack] 메시지 발송 완료");

        } catch (Exception e) {
            log.error("[Slack] 메시지 발송 실패: {}", e.getMessage(), e);
        }
    }
}
