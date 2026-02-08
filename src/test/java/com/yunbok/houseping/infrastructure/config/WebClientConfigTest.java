package com.yunbok.houseping.infrastructure.config;

import com.yunbok.houseping.config.WebClientConfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
        ReflectionTestUtils.setField(config, "applyHomeBaseUrl", "https://api.applyhome.co.kr");
        ReflectionTestUtils.setField(config, "applyHomeWebBaseUrl", "https://www.applyhome.co.kr");
        ReflectionTestUtils.setField(config, "lhBaseUrl", "https://api.lh.or.kr");
        ReflectionTestUtils.setField(config, "lhWebBaseUrl", "https://www.lh.or.kr");
        ReflectionTestUtils.setField(config, "botToken", "test-telegram-bot-token");
    }

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
