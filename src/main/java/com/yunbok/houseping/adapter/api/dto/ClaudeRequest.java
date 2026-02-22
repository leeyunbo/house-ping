package com.yunbok.houseping.adapter.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record ClaudeRequest(
        String model,
        @JsonProperty("max_tokens") int maxTokens,
        List<Map<String, String>> messages
) {
    public static ClaudeRequest of(String model, int maxTokens, String prompt) {
        return new ClaudeRequest(model, maxTokens, List.of(Map.of("role", "user", "content", prompt)));
    }
}
