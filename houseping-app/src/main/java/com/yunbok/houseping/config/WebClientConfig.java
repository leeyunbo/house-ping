package com.yunbok.houseping.config;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.netty.channel.ChannelOption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * WebClient 설정
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

    @Bean
    public HttpClient defaultHttpClient() {
        return HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5_000)
                .responseTimeout(Duration.ofSeconds(30));
    }

    private WebClient.Builder baseBuilder(HttpClient httpClient) {
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024));
    }

    @Bean
    public WebClient applyHomeWebClient(HttpClient defaultHttpClient) {
        return baseBuilder(defaultHttpClient)
                .baseUrl(applyHomeBaseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    public WebClient lhWebClient(HttpClient defaultHttpClient) {
        return baseBuilder(defaultHttpClient)
                .baseUrl(lhBaseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    public WebClient applyHomeWebCalendarClient(HttpClient defaultHttpClient) {
        return baseBuilder(defaultHttpClient)
                .baseUrl(applyHomeWebBaseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    public WebClient lhWebCalendarClient(HttpClient defaultHttpClient) {
        return baseBuilder(defaultHttpClient)
                .baseUrl(lhWebBaseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    public WebClient telegramWebClient(HttpClient defaultHttpClient) {
        return baseBuilder(defaultHttpClient)
                .baseUrl(TELEGRAM_API_BASE + botToken)
                .build();
    }

    @Bean
    public WebClient competitionRateWebClient(HttpClient defaultHttpClient) {
        return baseBuilder(defaultHttpClient)
                .baseUrl("https://api.odcloud.kr/api/ApplyhomeInfoCmpetRtSvc/v1")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    public WebClient claudeWebClient(HttpClient defaultHttpClient, @Value("${claude.api.key:}") String apiKey) {
        return baseBuilder(defaultHttpClient)
                .baseUrl("https://api.anthropic.com")
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", "2023-06-01")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    public WebClient realTransactionWebClient(HttpClient defaultHttpClient) {
        XmlMapper xmlMapper = new XmlMapper();

        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> {
                    configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024);
                    configurer.defaultCodecs().jackson2JsonDecoder(
                            new Jackson2JsonDecoder(xmlMapper, MediaType.APPLICATION_XML, MediaType.TEXT_XML)
                    );
                })
                .build();

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(defaultHttpClient))
                .baseUrl("https://apis.data.go.kr/1613000/RTMSDataSvcAptTradeDev")
                .exchangeStrategies(strategies)
                .build();
    }
}
