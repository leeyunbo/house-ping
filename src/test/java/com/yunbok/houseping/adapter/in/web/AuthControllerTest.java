package com.yunbok.houseping.adapter.in.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AuthController - 인증 컨트롤러")
class AuthControllerTest {

    private AuthController controller;

    @BeforeEach
    void setUp() {
        controller = new AuthController();
    }

    @Nested
    @DisplayName("loginPage() - 로그인 페이지")
    class LoginPage {

        @Test
        @DisplayName("로그인 페이지 뷰 이름을 반환한다")
        void returnsCorrectViewName() {
            // when
            String viewName = controller.loginPage();

            // then
            assertThat(viewName).isEqualTo("auth/login");
        }
    }

    @Nested
    @DisplayName("pendingPage() - 승인 대기 페이지")
    class PendingPage {

        @Test
        @DisplayName("승인 대기 페이지 뷰 이름을 반환한다")
        void returnsCorrectViewName() {
            // when
            String viewName = controller.pendingPage();

            // then
            assertThat(viewName).isEqualTo("auth/pending");
        }
    }
}
