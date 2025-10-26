package com.yunbok.houseping.infrastructure.adapter.outbound.api;

import com.yunbok.houseping.domain.model.SubscriptionInfo;
import com.yunbok.houseping.domain.port.outbound.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

/**
 * 텔레그램 알림 어댑터
 * ⭐ 요약 메시지에 청약Home + LH 데이터 소스별 구분 표시 추가
 * feature.notification.telegram-enabled=true 일 때만 활성화
 */
@Slf4j
@Component
@ConditionalOnProperty(
    name = "feature.notification.telegram-enabled",
    havingValue = "true",
    matchIfMissing = false
)
public class TelegramNotificationAdapter implements NotificationSender {

    private final WebClient webClient;

    @Value("${telegram.admin.chat.id}")
    private List<String> chatIds;

    public TelegramNotificationAdapter(@Qualifier("telegramWebClient") WebClient webClient) {
        this.webClient = webClient;
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
        String message = subscription.getDisplayMessage();
        sendTelegramMessage(message);
    }

    @Override
    public void sendErrorNotification(String errorMessage) {
        String message = "🚨 청약 알리미 오류 발생\n\n" + errorMessage;
        sendTelegramMessage(message);
    }

    @Override
    public void sendNotification(String message) {
        sendTelegramMessage(message);
    }

    private void sendBatchSummary(List<SubscriptionInfo> subscriptions) {
        StringBuilder summary = new StringBuilder();
        summary.append("🎉 오늘의 신규 청약 정보 ").append(subscriptions.size()).append("개\n\n");

        for (int i = 0; i < subscriptions.size(); i++) {
            SubscriptionInfo sub = subscriptions.get(i);
            summary.append(i + 1).append(". ").append(sub.getSimpleDisplayMessage());
        }

        sendTelegramMessage(summary.toString());
    }

    /**
     * 주택명에서 [LH] 태그 제거 (요약에서는 구분 표시로 충분)
     */
    private String cleanHouseName(String houseName) {
        return houseName.replace(" [LH]", "");
    }

    /**
     * 텔레그램 메시지 발송
     */
    private void sendTelegramMessage(String message) {
        for (String chatId : chatIds) {
            try {
                log.info("📨 텔레그램 메시지 발송 시도 - chatId: {}", chatId);

                Map<String, Object> request = Map.of(
                        "chat_id", chatId,
                        "text", message,
                        "parse_mode", "HTML"  // HTML 파싱으로 굵게, 기울임 등 지원
                );

                webClient.post()
                        .uri("/sendMessage")
                        .bodyValue(request)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block();

                log.info("📤 텔레그램 메시지 발송 완료");

            } catch (Exception e) {
                log.error("💥 텔레그램 메시지 발송 실패: {}", e.getMessage());
            }
        }
    }
}
