package com.yunbok.houseping.infrastructure.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WebClientConfig - WebClient 설정")
class WebClientConfigTest {

    private WebClientConfig config;

    @BeforeEach
    void setUp() {
        config = new WebClientConfig();
        // 테스트용 URL 설정
        ReflectionTestUtils.setField(config, "applyHomeBaseUrl", "https://api.applyhome.co.kr");
        ReflectionTestUtils.setField(config, "applyHomeWebBaseUrl", "https://www.applyhome.co.kr");
        ReflectionTestUtils.setField(config, "lhBaseUrl", "https://api.lh.or.kr");
        ReflectionTestUtils.setField(config, "lhWebBaseUrl", "https://www.lh.or.kr");
        ReflectionTestUtils.setField(config, "botToken", "test-telegram-bot-token");
    }

    @Nested
    @DisplayName("applyHomeWebClient() - 청약Home API용 WebClient")
    class ApplyHomeWebClient {

        @Test
        @DisplayName("WebClient 인스턴스를 생성한다")
        void createsWebClient() {
            // when
            WebClient webClient = config.applyHomeWebClient();

            // then
            assertThat(webClient).isNotNull();
        }
    }

    @Nested
    @DisplayName("lhWebClient() - LH API용 WebClient")
    class LhWebClient {

        @Test
        @DisplayName("WebClient 인스턴스를 생성한다")
        void createsWebClient() {
            // when
            WebClient webClient = config.lhWebClient();

            // then
            assertThat(webClient).isNotNull();
        }
    }

    @Nested
    @DisplayName("applyHomeWebCalendarClient() - 청약Home 웹 캘린더용 WebClient")
    class ApplyHomeWebCalendarClient {

        @Test
        @DisplayName("WebClient 인스턴스를 생성한다")
        void createsWebClient() {
            // when
            WebClient webClient = config.applyHomeWebCalendarClient();

            // then
            assertThat(webClient).isNotNull();
        }
    }

    @Nested
    @DisplayName("lhWebCalendarClient() - LH 웹 캘린더용 WebClient")
    class LhWebCalendarClient {

        @Test
        @DisplayName("WebClient 인스턴스를 생성한다")
        void createsWebClient() {
            // when
            WebClient webClient = config.lhWebCalendarClient();

            // then
            assertThat(webClient).isNotNull();
        }
    }

    @Nested
    @DisplayName("telegramWebClient() - 텔레그램용 WebClient")
    class TelegramWebClient {

        @Test
        @DisplayName("WebClient 인스턴스를 생성한다")
        void createsWebClient() {
            // when
            WebClient webClient = config.telegramWebClient();

            // then
            assertThat(webClient).isNotNull();
        }
    }

    @Nested
    @DisplayName("WebClient 공통 설정")
    class CommonSettings {

        @Test
        @DisplayName("각 WebClient는 독립적인 인스턴스이다")
        void createsIndependentInstances() {
            // when
            WebClient applyHome = config.applyHomeWebClient();
            WebClient lh = config.lhWebClient();
            WebClient applyHomeCalendar = config.applyHomeWebCalendarClient();
            WebClient lhCalendar = config.lhWebCalendarClient();
            WebClient telegram = config.telegramWebClient();

            // then
            assertThat(applyHome).isNotSameAs(lh);
            assertThat(lh).isNotSameAs(applyHomeCalendar);
            assertThat(applyHomeCalendar).isNotSameAs(lhCalendar);
            assertThat(lhCalendar).isNotSameAs(telegram);
        }
    }
}
