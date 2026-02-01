package com.yunbok.houseping.infrastructure.security;

import com.yunbok.houseping.domain.model.User;
import com.yunbok.houseping.domain.model.UserRole;
import com.yunbok.houseping.domain.model.UserStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.util.HashMap;

import static org.mockito.Mockito.*;

@DisplayName("OAuth2AuthenticationSuccessHandler - 로그인 성공 핸들러")
@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationSuccessHandlerTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    private OAuth2AuthenticationSuccessHandler handler;

    @BeforeEach
    void setUp() {
        handler = new OAuth2AuthenticationSuccessHandler();
    }

    @Nested
    @DisplayName("onAuthenticationSuccess() - 인증 성공 처리")
    class OnAuthenticationSuccess {

        @Test
        @DisplayName("ACTIVE 사용자는 홈으로 리다이렉트한다")
        void redirectsActiveToHome() throws IOException {
            // given
            User activeUser = User.builder()
                    .id(1L)
                    .naverId("naver-123")
                    .name("활성사용자")
                    .role(UserRole.USER)
                    .status(UserStatus.ACTIVE)
                    .build();
            CustomOAuth2User oAuth2User = new CustomOAuth2User(activeUser, new HashMap<>());
            when(authentication.getPrincipal()).thenReturn(oAuth2User);

            // when
            handler.onAuthenticationSuccess(request, response, authentication);

            // then
            verify(response).sendRedirect("/home");
        }

        @Test
        @DisplayName("PENDING 사용자도 홈으로 리다이렉트한다")
        void redirectsPendingToHome() throws IOException {
            // given
            User pendingUser = User.builder()
                    .id(1L)
                    .naverId("naver-123")
                    .name("대기사용자")
                    .role(UserRole.USER)
                    .status(UserStatus.PENDING)
                    .build();
            CustomOAuth2User oAuth2User = new CustomOAuth2User(pendingUser, new HashMap<>());
            when(authentication.getPrincipal()).thenReturn(oAuth2User);

            // when
            handler.onAuthenticationSuccess(request, response, authentication);

            // then
            verify(response).sendRedirect("/home");
        }

        @Test
        @DisplayName("MASTER 사용자도 홈으로 리다이렉트한다")
        void redirectsMasterToHome() throws IOException {
            // given
            User masterUser = User.builder()
                    .id(1L)
                    .naverId("naver-master")
                    .name("마스터")
                    .role(UserRole.MASTER)
                    .status(UserStatus.ACTIVE)
                    .build();
            CustomOAuth2User oAuth2User = new CustomOAuth2User(masterUser, new HashMap<>());
            when(authentication.getPrincipal()).thenReturn(oAuth2User);

            // when
            handler.onAuthenticationSuccess(request, response, authentication);

            // then
            verify(response).sendRedirect("/home");
        }
    }
}
