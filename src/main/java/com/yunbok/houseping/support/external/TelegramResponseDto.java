package com.yunbok.houseping.support.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

/**
 * 텔레그램 API 응답 DTO
 */
@Value
@Builder
public class TelegramResponseDto {
    boolean ok;
    String description;
    ResultDto result;

    @Value
    @Builder
    public static class ResultDto {
        @JsonProperty("message_id")
        Long messageId;

        String text;
        Long date;
    }
}
