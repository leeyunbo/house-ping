package com.yunbok.houseping.infrastructure.config;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
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

    @Value("${applyhome.web.base-url}")
    private String applyHomeWebBaseUrl;

    @Value("${lh.api.base-url}")
    private String lhBaseUrl;

    @Value("${lh.web.base-url}")
    private String lhWebBaseUrl;

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

    /**
     * 청약Home 웹 캘린더용 WebClient (API 다운 시 대체)
     */
    @Bean
    public WebClient applyHomeWebCalendarClient() {
        return WebClient.builder()
                .baseUrl(applyHomeWebBaseUrl)
                .defaultHeader("Content-Type", "application/json")
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    /**
     * LH 웹 캘린더용 WebClient (API 다운 시 대체)
     */
    @Bean
    public WebClient lhWebCalendarClient() {
        return WebClient.builder()
                .baseUrl(lhWebBaseUrl)
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

    /**
     * 청약홈 경쟁률 API용 WebClient
     */
    @Bean
    public WebClient competitionRateWebClient() {
        return WebClient.builder()
                .baseUrl("https://api.odcloud.kr/api/ApplyhomeInfoCmpetRtSvc/v1")
                .defaultHeader("Content-Type", "application/json")
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    /**
     * 국토교통부 실거래가 API용 WebClient
     * XML 응답을 Jackson으로 파싱
     */
    @Bean
    public WebClient realTransactionWebClient() {
        // Jackson XmlMapper로 XML 처리
        XmlMapper xmlMapper = new XmlMapper();

        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> {
                    configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024);
                    // XML 지원 추가 (application/xml, text/xml 등)
                    configurer.defaultCodecs().jackson2JsonDecoder(
                            new Jackson2JsonDecoder(xmlMapper, MediaType.APPLICATION_XML, MediaType.TEXT_XML)
                    );
                })
                .build();

        return WebClient.builder()
                .baseUrl("https://apis.data.go.kr/1613000/RTMSDataSvcAptTradeDev")
                .exchangeStrategies(strategies)
                .build();
    }
}
