package com.yunbok.houseping.adapter.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class ClaudeApiAdapter {

    private final WebClient claudeWebClient;
    private final String model;

    public ClaudeApiAdapter(@Qualifier("claudeWebClient") WebClient claudeWebClient,
                            @Value("${claude.api.model:claude-sonnet-4-20250514}") String model) {
        this.claudeWebClient = claudeWebClient;
        this.model = model;
    }

    public Optional<String> generateBlogContent(String prompt) {
        try {
            Map<String, Object> request = Map.of(
                    "model", model,
                    "max_tokens", 4096,
                    "messages", List.of(Map.of("role", "user", "content", prompt))
            );

            Map<?, ?> response = claudeWebClient.post()
                    .uri("/v1/messages")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null || !response.containsKey("content")) {
                log.warn("[Claude API] 응답에 content가 없습니다: {}", response);
                return Optional.empty();
            }

            List<?> contentList = (List<?>) response.get("content");
            if (contentList.isEmpty()) {
                return Optional.empty();
            }

            Map<?, ?> firstBlock = (Map<?, ?>) contentList.get(0);
            String text = (String) firstBlock.get("text");
            return Optional.ofNullable(text);
        } catch (Exception e) {
            log.error("[Claude API] 호출 실패", e);
            return Optional.empty();
        }
    }
}
