package com.yunbok.houseping.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 청약 관련 설정을 외부화하는 Properties 클래스
 */
@Configuration
@ConfigurationProperties(prefix = "subscription")
@Getter
@Setter
public class SubscriptionProperties {

    /**
     * 수집 대상 지역 목록
     */
    private List<String> targetAreas = List.of("서울", "경기");

    /**
     * API 관련 설정
     */
    private ApiProperties api = new ApiProperties();

    @Getter
    @Setter
    public static class ApiProperties {
        /**
         * API 페이지 사이즈
         */
        private int pageSize = 5000;

        /**
         * 기본 페이지 번호
         */
        private int defaultPage = 1;
    }
}
