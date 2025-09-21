package com.yunbok.houseping.infrastructure.dto;

import lombok.Builder;
import lombok.Value;

/**
 * 텔레그램 명령어 처리 DTO
 */
@Value
@Builder
public class TelegramCommandDto {
    String chatId;
    String command;
    String username;
    String firstName;
    String lastName;
    
    public String getDisplayName() {
        if (username != null && !username.isEmpty()) {
            return "@" + username;
        }
        if (firstName != null && !firstName.isEmpty()) {
            return firstName;
        }
        return "익명사용자";
    }
}
