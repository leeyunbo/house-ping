package com.yunbok.houseping.support.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 텔레그램 Webhook 요청 DTO
 */
@Data
public class TelegramWebhookDto {

    @JsonProperty("update_id")
    private Long updateId;

    private MessageDto message;

    @Data
    public static class MessageDto {
        @JsonProperty("message_id")
        private Long messageId;

        private UserDto from;
        private ChatDto chat;
        private String text;
        private Long date;
    }

    @Data
    public static class UserDto {
        private Long id;
        private String username;

        @JsonProperty("first_name")
        private String firstName;

        @JsonProperty("last_name")
        private String lastName;

        @JsonProperty("is_bot")
        private Boolean isBot;
    }

    @Data
    public static class ChatDto {
        private Long id;
        private String type; // "private", "group", "supergroup", "channel"
        private String username;

        @JsonProperty("first_name")
        private String firstName;

        @JsonProperty("last_name")
        private String lastName;
    }
}
