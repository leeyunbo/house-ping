package com.yunbok.houseping.infrastructure.api;

import com.yunbok.houseping.core.domain.Subscription;
import com.yunbok.houseping.infrastructure.formatter.TelegramMessageFormatter;
import com.yunbok.houseping.support.dto.DailyNotificationReport;
import com.yunbok.houseping.support.external.TelegramResponseDto;
import com.yunbok.houseping.support.external.TelegramSendMessageRequest;
import com.yunbok.houseping.core.port.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

/**
 * 텔레그램 알림 어댑터
 * feature.notification.telegram-enabled=true 일 때만 활성화
 */
@Slf4j
@Component
@ConditionalOnProperty(
    name = "feature.notification.telegram-enabled",
    havingValue = "true"
)
public class TelegramNotificationClient implements NotificationSender {

    private final WebClient webClient;
    private final TelegramMessageFormatter messageFormatter;

    @Value("${telegram.admin.chat.id}")
    private List<String> chatIds;

    public TelegramNotificationClient(
            @Qualifier("telegramWebClient") WebClient webClient,
            TelegramMessageFormatter messageFormatter) {
        this.webClient = webClient;
        this.messageFormatter = messageFormatter;
    }

    public void sendNewSubscriptions(List<Subscription> subscriptions) {
        if (subscriptions.isEmpty()) {
            return;
        }
        String summaryMessage = messageFormatter.formatBatchSummary(subscriptions);
        sendTelegramMessage(summaryMessage);

        subscriptions.forEach(this::sendSubscription);
    }

    public void sendSubscription(Subscription subscription) {
        String message = messageFormatter.formatSubscription(subscription);
        sendTelegramMessage(message);
    }

    public void sendErrorNotification(String errorMessage) {
        String message = messageFormatter.formatErrorMessage(errorMessage);
        sendTelegramMessage(message);
    }

    public void sendNotification(String message) {
        sendTelegramMessage(message);
    }

    public void sendNoDataNotification() {
        sendTelegramMessage(messageFormatter.formatNoDataMessage());
    }

    public void sendDailyReport(DailyNotificationReport report) {
        String message = messageFormatter.formatDailyReport(report);
        sendTelegramMessage(message);
    }

    /**
     * 텔레그램 메시지 발송
     */
    private void sendTelegramMessage(String message) {
        for (String chatId : chatIds) {
            try {
                log.info("[Telegram] 메시지 발송 시도 - chatId: {}", chatId);

                webClient.post()
                        .uri("/sendMessage")
                        .bodyValue(TelegramSendMessageRequest.html(chatId, message))
                        .retrieve()
                        .bodyToMono(TelegramResponseDto.class)
                        .block();

                log.info("[Telegram] 메시지 발송 완료");

            } catch (Exception e) {
                log.error("[Telegram] 메시지 발송 실패: {}", e.getMessage());
            }
        }
    }
}
