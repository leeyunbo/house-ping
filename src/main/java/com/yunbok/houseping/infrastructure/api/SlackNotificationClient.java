package com.yunbok.houseping.infrastructure.api;

import com.yunbok.houseping.core.domain.Subscription;
import com.yunbok.houseping.infrastructure.formatter.SlackMessageFormatter;
import com.yunbok.houseping.support.dto.DailyNotificationReport;
import com.yunbok.houseping.core.port.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.yunbok.houseping.infrastructure.api.dto.SlackWebhookRequest;

import java.util.List;

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
public class SlackNotificationClient implements NotificationSender {

    private final WebClient webClient;
    private final String webhookUrl;
    private final SlackMessageFormatter messageFormatter;

    public SlackNotificationClient(
            @Value("${slack.webhook.url}") String webhookUrl,
            SlackMessageFormatter messageFormatter) {
        this.webhookUrl = webhookUrl;
        this.webClient = WebClient.create();
        this.messageFormatter = messageFormatter;
    }

    public void sendNewSubscriptions(List<Subscription> subscriptions) {
        if (subscriptions.isEmpty()) {
            return;
        }
        String summaryMessage = messageFormatter.formatBatchSummary(subscriptions);
        sendSlackMessage(summaryMessage);

        subscriptions.forEach(this::sendSubscription);
    }

    public void sendSubscription(Subscription subscription) {
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

            webClient.post()
                    .uri(webhookUrl)
                    .bodyValue(SlackWebhookRequest.of(message))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("[Slack] 메시지 발송 완료");

        } catch (Exception e) {
            log.error("[Slack] 메시지 발송 실패: {}", e.getMessage(), e);
        }
    }
}
