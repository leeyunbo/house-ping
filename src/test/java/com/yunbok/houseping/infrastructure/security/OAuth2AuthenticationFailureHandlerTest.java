package com.yunbok.houseping.infrastructure.security;

import com.yunbok.houseping.config.oauth2.OAuth2AuthenticationFailureHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;

import static org.mockito.Mockito.*;

@DisplayName("OAuth2AuthenticationFailureHandler - 로그인 실패 핸들러")
@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationFailureHandlerTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AuthenticationException exception;

    private OAuth2AuthenticationFailureHandler handler;

    @BeforeEach
    void setUp() {
        handler = new OAuth2AuthenticationFailureHandler();
    }

    @Nested
    @DisplayName("onAuthenticationFailure() - 인증 실패 처리")
    class OnAuthenticationFailure {

        @Test
        @DisplayName("로그인 페이지로 에러 파라미터와 함께 리다이렉트한다")
        void redirectsToLoginWithError() throws IOException {
            // when
            handler.onAuthenticationFailure(request, response, exception);

            // then
            verify(response).sendRedirect("/auth/login?error");
        }
    }
}
