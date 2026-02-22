package com.yunbok.houseping.adapter.api.dto;

import java.util.List;

public record ClaudeResponse(List<ContentBlock> content) {

    public record ContentBlock(String type, String text) {}

    public String getFirstText() {
        if (content == null || content.isEmpty()) {
            return null;
        }
        return content.get(0).text();
    }
}
