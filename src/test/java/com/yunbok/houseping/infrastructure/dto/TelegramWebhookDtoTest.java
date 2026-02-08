package com.yunbok.houseping.infrastructure.dto;

import com.yunbok.houseping.support.external.TelegramWebhookDto;
import com.yunbok.houseping.support.external.TelegramCommandDto;
import com.yunbok.houseping.support.external.TelegramResponseDto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TelegramWebhookDto - 텔레그램 Webhook 요청 DTO")
class TelegramWebhookDtoTest {

    @Nested
    @DisplayName("TelegramWebhookDto - 메인 DTO")
    class MainDto {

        @Test
        @DisplayName("updateId를 설정하고 조회할 수 있다")
        void canSetAndGetUpdateId() {
            // given
            TelegramWebhookDto dto = new TelegramWebhookDto();

            // when
            dto.setUpdateId(12345L);

            // then
            assertThat(dto.getUpdateId()).isEqualTo(12345L);
        }

        @Test
        @DisplayName("message를 설정하고 조회할 수 있다")
        void canSetAndGetMessage() {
            // given
            TelegramWebhookDto dto = new TelegramWebhookDto();
            TelegramWebhookDto.MessageDto message = new TelegramWebhookDto.MessageDto();
            message.setText("테스트 메시지");

            // when
            dto.setMessage(message);

            // then
            assertThat(dto.getMessage()).isNotNull();
            assertThat(dto.getMessage().getText()).isEqualTo("테스트 메시지");
        }
    }

    @Nested
    @DisplayName("MessageDto - 메시지 DTO")
    class MessageDtoTest {

        @Test
        @DisplayName("모든 필드를 설정하고 조회할 수 있다")
        void canSetAndGetAllFields() {
            // given
            TelegramWebhookDto.MessageDto message = new TelegramWebhookDto.MessageDto();

            // when
            message.setMessageId(100L);
            message.setText("Hello World");
            message.setDate(1234567890L);

            // then
            assertThat(message.getMessageId()).isEqualTo(100L);
            assertThat(message.getText()).isEqualTo("Hello World");
            assertThat(message.getDate()).isEqualTo(1234567890L);
        }

        @Test
        @DisplayName("from과 chat을 설정할 수 있다")
        void canSetFromAndChat() {
            // given
            TelegramWebhookDto.MessageDto message = new TelegramWebhookDto.MessageDto();
            TelegramWebhookDto.UserDto user = new TelegramWebhookDto.UserDto();
            TelegramWebhookDto.ChatDto chat = new TelegramWebhookDto.ChatDto();

            user.setId(1L);
            chat.setId(2L);

            // when
            message.setFrom(user);
            message.setChat(chat);

            // then
            assertThat(message.getFrom()).isNotNull();
            assertThat(message.getFrom().getId()).isEqualTo(1L);
            assertThat(message.getChat()).isNotNull();
            assertThat(message.getChat().getId()).isEqualTo(2L);
        }
    }

    @Nested
    @DisplayName("UserDto - 사용자 DTO")
    class UserDtoTest {

        @Test
        @DisplayName("모든 필드를 설정하고 조회할 수 있다")
        void canSetAndGetAllFields() {
            // given
            TelegramWebhookDto.UserDto user = new TelegramWebhookDto.UserDto();

            // when
            user.setId(12345L);
            user.setUsername("testuser");
            user.setFirstName("Test");
            user.setLastName("User");
            user.setIsBot(false);

            // then
            assertThat(user.getId()).isEqualTo(12345L);
            assertThat(user.getUsername()).isEqualTo("testuser");
            assertThat(user.getFirstName()).isEqualTo("Test");
            assertThat(user.getLastName()).isEqualTo("User");
            assertThat(user.getIsBot()).isFalse();
        }
    }

    @Nested
    @DisplayName("ChatDto - 채팅 DTO")
    class ChatDtoTest {

        @Test
        @DisplayName("모든 필드를 설정하고 조회할 수 있다")
        void canSetAndGetAllFields() {
            // given
            TelegramWebhookDto.ChatDto chat = new TelegramWebhookDto.ChatDto();

            // when
            chat.setId(67890L);
            chat.setType("private");
            chat.setUsername("testchat");
            chat.setFirstName("Chat");
            chat.setLastName("Name");

            // then
            assertThat(chat.getId()).isEqualTo(67890L);
            assertThat(chat.getType()).isEqualTo("private");
            assertThat(chat.getUsername()).isEqualTo("testchat");
            assertThat(chat.getFirstName()).isEqualTo("Chat");
            assertThat(chat.getLastName()).isEqualTo("Name");
        }

        @Test
        @DisplayName("채팅 타입은 private, group, supergroup, channel 중 하나이다")
        void chatTypeCanBeVariousTypes() {
            // given
            TelegramWebhookDto.ChatDto chat = new TelegramWebhookDto.ChatDto();

            // when & then
            chat.setType("private");
            assertThat(chat.getType()).isEqualTo("private");

            chat.setType("group");
            assertThat(chat.getType()).isEqualTo("group");

            chat.setType("supergroup");
            assertThat(chat.getType()).isEqualTo("supergroup");

            chat.setType("channel");
            assertThat(chat.getType()).isEqualTo("channel");
        }
    }
}
