package com.yunbok.houseping.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OAuth2UserInfo - OAuth2 사용자 정보")
class OAuth2UserInfoTest {

    @Nested
    @DisplayName("Builder - OAuth2 사용자 정보 생성")
    class Builder {

        @Test
        @DisplayName("모든 필드가 올바르게 설정된다")
        void setsAllFieldsCorrectly() {
            // when
            OAuth2UserInfo userInfo = OAuth2UserInfo.builder()
                    .naverId("naver-id-123")
                    .email("user@example.com")
                    .name("홍길동")
                    .build();

            // then
            assertThat(userInfo.getNaverId()).isEqualTo("naver-id-123");
            assertThat(userInfo.getEmail()).isEqualTo("user@example.com");
            assertThat(userInfo.getName()).isEqualTo("홍길동");
        }

        @Test
        @DisplayName("null 값도 허용된다")
        void allowsNullValues() {
            // when
            OAuth2UserInfo userInfo = OAuth2UserInfo.builder()
                    .naverId("naver-id-123")
                    .email(null)
                    .name(null)
                    .build();

            // then
            assertThat(userInfo.getNaverId()).isEqualTo("naver-id-123");
            assertThat(userInfo.getEmail()).isNull();
            assertThat(userInfo.getName()).isNull();
        }
    }
}
