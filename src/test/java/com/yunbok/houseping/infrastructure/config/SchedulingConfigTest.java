package com.yunbok.houseping.infrastructure.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.EnableScheduling;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SchedulingConfig - 스케줄링 설정")
class SchedulingConfigTest {

    @Test
    @DisplayName("EnableScheduling 어노테이션이 적용되어 있다")
    void hasEnableSchedulingAnnotation() {
        // given
        Class<SchedulingConfig> configClass = SchedulingConfig.class;

        // then
        assertThat(configClass.isAnnotationPresent(EnableScheduling.class)).isTrue();
    }

    @Test
    @DisplayName("Configuration 클래스로 등록된다")
    void isConfigurationClass() {
        // given
        Class<SchedulingConfig> configClass = SchedulingConfig.class;

        // then
        assertThat(configClass.isAnnotationPresent(org.springframework.context.annotation.Configuration.class))
                .isTrue();
    }

    @Test
    @DisplayName("인스턴스 생성이 가능하다")
    void canCreateInstance() {
        // when
        SchedulingConfig config = new SchedulingConfig();

        // then
        assertThat(config).isNotNull();
    }
}
