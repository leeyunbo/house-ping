package com.yunbok.houseping.infrastructure.adapter.outbound.api;

import com.yunbok.houseping.domain.model.SubscriptionInfo;
import com.yunbok.houseping.domain.port.outbound.NotificationSender;
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

    public SlackNotificationAdapter(@Value("${slack.webhook.url}") String webhookUrl) {
        this.webhookUrl = webhookUrl;
        this.webClient = WebClient.create();
    }

    @Override
    public void sendNewSubscriptions(List<SubscriptionInfo> subscriptions) {
        if (subscriptions.isEmpty()) {
            return;
        }
        sendBatchSummary(subscriptions);
        subscriptions.forEach(this::sendSubscription);
    }

    @Override
    public void sendSubscription(SubscriptionInfo subscription) {
        String message = formatSubscriptionMessage(subscription);
        sendSlackMessage(message);
    }

    @Override
    public void sendErrorNotification(String errorMessage) {
        String message = ":rotating_light: *청약 알리미 오류 발생*\n\n" + errorMessage;
        sendSlackMessage(message);
    }

    @Override
    public void sendNotification(String message) {
        sendSlackMessage(message);
    }

    /**
     * 배치 요약 메시지 발송
     */
    private void sendBatchSummary(List<SubscriptionInfo> subscriptions) {
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

        sendSlackMessage(summary.toString());
    }

    /**
     * 청약 정보를 Slack 메시지 형식으로 변환
     */
    private String formatSubscriptionMessage(SubscriptionInfo subscription) {
        String displayMessage = subscription.getDisplayMessage();

        // Markdown 형식을 Slack mrkdwn 형식으로 변환
        // 청약 정보의 이모지는 그대로 사용 가능
        return displayMessage;
    }

    /**
     * Slack Webhook으로 메시지 발송
     */
    private void sendSlackMessage(String message) {
        try {
            log.info("📨 Slack 메시지 발송 시도");

            Map<String, Object> request = Map.of(
                "text", message,
                "mrkdwn", true
            );

            webClient.post()
                    .uri(webhookUrl)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("📤 Slack 메시지 발송 완료");

        } catch (Exception e) {
            log.error("💥 Slack 메시지 발송 실패: {}", e.getMessage(), e);
        }
    }
}
