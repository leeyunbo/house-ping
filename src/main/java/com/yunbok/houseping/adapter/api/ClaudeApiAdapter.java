package com.yunbok.houseping.adapter.api;

import com.yunbok.houseping.adapter.api.dto.ClaudeRequest;
import com.yunbok.houseping.adapter.api.dto.ClaudeResponse;
import com.yunbok.houseping.support.exception.ClaudeApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class ClaudeApiAdapter {

    private final WebClient claudeWebClient;
    private final String model;
    private final int maxTokens;

    public ClaudeApiAdapter(@Qualifier("claudeWebClient") WebClient claudeWebClient,
                            @Value("${claude.api.model:claude-sonnet-4-20250514}") String model,
                            @Value("${claude.api.max-tokens:4096}") int maxTokens) {
        this.claudeWebClient = claudeWebClient;
        this.model = model;
        this.maxTokens = maxTokens;
    }

    public String generateBlogContent(String prompt) {
        try {
            ClaudeRequest request = ClaudeRequest.of(model, maxTokens, prompt);

            ClaudeResponse response = claudeWebClient.post()
                    .uri("/v1/messages")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(ClaudeResponse.class)
                    .block();

            if (response == null) {
                throw new ClaudeApiException("응답이 null입니다");
            }

            String text = response.getFirstText();
            if (text == null) {
                throw new ClaudeApiException("응답에 텍스트가 없습니다");
            }

            return text;
        } catch (ClaudeApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Claude API] 호출 실패", e);
            throw new ClaudeApiException(e);
        }
    }
}
