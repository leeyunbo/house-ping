package com.yunbok.houseping.infrastructure.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SubscriptionProperties - 청약 관련 설정")
class SubscriptionPropertiesTest {

    private SubscriptionProperties properties;

    @BeforeEach
    void setUp() {
        properties = new SubscriptionProperties();
    }

    @Nested
    @DisplayName("targetAreas - 수집 대상 지역")
    class TargetAreas {

        @Test
        @DisplayName("기본값은 서울, 경기이다")
        void hasDefaultAreas() {
            // then
            assertThat(properties.getTargetAreas()).containsExactly("서울", "경기");
        }

        @Test
        @DisplayName("대상 지역을 변경할 수 있다")
        void canSetTargetAreas() {
            // given
            List<String> newAreas = List.of("서울", "경기", "인천", "대전");

            // when
            properties.setTargetAreas(newAreas);

            // then
            assertThat(properties.getTargetAreas()).containsExactly("서울", "경기", "인천", "대전");
        }
    }

    @Nested
    @DisplayName("api - API 관련 설정")
    class ApiSettings {

        @Test
        @DisplayName("기본 API 속성 객체가 존재한다")
        void hasDefaultApiProperties() {
            // then
            assertThat(properties.getApi()).isNotNull();
        }

        @Test
        @DisplayName("API 속성을 변경할 수 있다")
        void canSetApiProperties() {
            // given
            SubscriptionProperties.ApiProperties newApi = new SubscriptionProperties.ApiProperties();
            newApi.setPageSize(1000);
            newApi.setDefaultPage(2);

            // when
            properties.setApi(newApi);

            // then
            assertThat(properties.getApi().getPageSize()).isEqualTo(1000);
            assertThat(properties.getApi().getDefaultPage()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("ApiProperties - API 속성")
    class ApiPropertiesTest {

        private SubscriptionProperties.ApiProperties apiProperties;

        @BeforeEach
        void setUp() {
            apiProperties = new SubscriptionProperties.ApiProperties();
        }

        @Test
        @DisplayName("기본 페이지 사이즈는 5000이다")
        void hasDefaultPageSize() {
            // then
            assertThat(apiProperties.getPageSize()).isEqualTo(5000);
        }

        @Test
        @DisplayName("기본 페이지 번호는 1이다")
        void hasDefaultPage() {
            // then
            assertThat(apiProperties.getDefaultPage()).isEqualTo(1);
        }

        @Test
        @DisplayName("페이지 사이즈를 변경할 수 있다")
        void canSetPageSize() {
            // when
            apiProperties.setPageSize(100);

            // then
            assertThat(apiProperties.getPageSize()).isEqualTo(100);
        }

        @Test
        @DisplayName("기본 페이지 번호를 변경할 수 있다")
        void canSetDefaultPage() {
            // when
            apiProperties.setDefaultPage(5);

            // then
            assertThat(apiProperties.getDefaultPage()).isEqualTo(5);
        }
    }
}
