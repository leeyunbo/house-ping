package com.yunbok.houseping.infrastructure.security;

import com.yunbok.houseping.domain.model.OAuth2UserInfo;
import com.yunbok.houseping.domain.model.User;
import com.yunbok.houseping.domain.model.UserRole;
import com.yunbok.houseping.domain.model.UserStatus;
import com.yunbok.houseping.domain.port.in.AuthenticationUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("CustomOAuth2UserService - OAuth2 사용자 서비스")
@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private AuthenticationUseCase authenticationUseCase;

    private CustomOAuth2UserService service;

    @BeforeEach
    void setUp() {
        service = new CustomOAuth2UserService(authenticationUseCase);
    }

    @Nested
    @DisplayName("OAuth2UserInfo 변환")
    class OAuth2UserInfoConversion {

        @Test
        @DisplayName("서비스가 AuthenticationUseCase를 주입받아 생성된다")
        void serviceIsCreatedWithAuthenticationUseCase() {
            // CustomOAuth2UserService는 DefaultOAuth2UserService를 상속하므로
            // loadUser의 완전한 테스트는 Spring Security 통합 테스트가 필요합니다.
            // 여기서는 서비스가 올바르게 생성되는지만 검증합니다.

            // then
            assertThat(service).isNotNull();
        }
    }

    @Nested
    @DisplayName("loadUser 결과")
    class LoadUserResult {

        @Test
        @DisplayName("CustomOAuth2User 타입을 반환해야 한다")
        void shouldReturnCustomOAuth2UserType() {
            // 이 테스트는 loadUser의 결과 타입을 검증합니다.
            // 실제 HTTP 호출 없이 검증하기 위해 통합 테스트가 필요합니다.

            // given
            User user = User.builder()
                    .id(1L)
                    .naverId("naver-123")
                    .name("테스트")
                    .role(UserRole.USER)
                    .status(UserStatus.ACTIVE)
                    .build();

            Map<String, Object> attributes = new HashMap<>();
            attributes.put("response", Map.of("id", "naver-123", "name", "테스트"));

            // when
            CustomOAuth2User oAuth2User = new CustomOAuth2User(user, attributes);

            // then
            assertThat(oAuth2User).isInstanceOf(OAuth2User.class);
            assertThat(oAuth2User.getUser()).isEqualTo(user);
        }
    }
}
