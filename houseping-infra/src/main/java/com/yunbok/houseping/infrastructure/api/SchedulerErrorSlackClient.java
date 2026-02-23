package com.yunbok.houseping.infrastructure.api;

import com.yunbok.houseping.infrastructure.api.dto.SlackWebhookRequest;
import com.yunbok.houseping.infrastructure.formatter.SlackMessageFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * 스케줄러 실패 전용 Slack 알림 컴포넌트
 * slack.webhook.error-url이 비어있으면 알림을 보내지 않음
 */
@Slf4j
@Component
public class SchedulerErrorSlackClient {

    private final WebClient webClient;
    private final String errorWebhookUrl;
    private final SlackMessageFormatter messageFormatter;

    public SchedulerErrorSlackClient(
            @Value("${slack.webhook.error-url:}") String errorWebhookUrl,
            SlackMessageFormatter messageFormatter) {
        this.errorWebhookUrl = errorWebhookUrl;
        this.messageFormatter = messageFormatter;
        this.webClient = WebClient.create();
    }

    public void sendError(String schedulerName, Exception e) {
        if (errorWebhookUrl == null || errorWebhookUrl.isBlank()) {
            return;
        }
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String stackTrace = summarizeStackTrace(e, 5);

            String message = messageFormatter.formatSchedulerError(schedulerName, timestamp, e.getMessage(), stackTrace);
            String fallback = messageFormatter.formatSchedulerErrorFallback(schedulerName, timestamp, e.getMessage());

            sendSlackMessage(message, fallback);
            log.info("[에러 알림] {} 실패 알림 발송 완료", schedulerName);
        } catch (Exception sendError) {
            log.error("[에러 알림] 알림 발송 실패: {}", sendError.getMessage());
        }
    }

    private void sendSlackMessage(String message, String fallbackText) {
        webClient.post()
                .uri(errorWebhookUrl)
                .bodyValue(SlackWebhookRequest.of(message, fallbackText))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private String summarizeStackTrace(Exception e, int maxLines) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String[] lines = sw.toString().split("\n");
        int limit = Math.min(lines.length, maxLines);
        return String.join("\n", Arrays.copyOf(lines, limit));
    }
}
