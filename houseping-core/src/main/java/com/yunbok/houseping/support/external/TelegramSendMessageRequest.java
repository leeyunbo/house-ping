package com.yunbok.houseping.support.external;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TelegramSendMessageRequest(
    @JsonProperty("chat_id") String chatId,
    @JsonProperty("text") String text,
    @JsonProperty("parse_mode") String parseMode
) {
    public static TelegramSendMessageRequest html(String chatId, String text) {
        return new TelegramSendMessageRequest(chatId, text, "HTML");
    }
}
