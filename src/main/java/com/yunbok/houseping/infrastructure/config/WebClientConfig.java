package com.yunbok.houseping.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClient 설정
 * ⭐ LH API용 WebClient 추가 (기존 청약Home WebClient는 그대로 유지)
 */
@Slf4j
@Configuration
public class WebClientConfig {

    @Value("${applyhome.api.base-url}")
    private String applyHomeBaseUrl;

    @Value("${lh.api.base-url}")
    private String lhBaseUrl;

    @Value("${telegram.bot.token}")
    private String botToken;

    private static final String TELEGRAM_API_BASE = "https://api.telegram.org/bot";

    /**
     * 기존 청약Home API용 WebClient (변경 없음)
     */
    @Bean
    public WebClient applyHomeWebClient() {
        return WebClient.builder()
                .baseUrl(applyHomeBaseUrl)
                .defaultHeader("Content-Type", "application/json")
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    /**
     * LH API용 WebClient (새로 추가)
     */
    @Bean
    public WebClient lhWebClient() {
        return WebClient.builder()
                .baseUrl(lhBaseUrl)
                .defaultHeader("Content-Type", "application/json")
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    @Bean
    public WebClient telegramWebClient() {
        return WebClient.builder()
                .baseUrl(TELEGRAM_API_BASE + botToken)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }
}
