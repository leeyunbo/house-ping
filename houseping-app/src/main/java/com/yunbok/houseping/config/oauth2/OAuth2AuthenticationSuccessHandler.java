package com.yunbok.houseping.config.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2AuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    public OAuth2AuthenticationSuccessHandler() {
        // 저장된 요청이 없을 때 기본 리다이렉트 URL
        setDefaultTargetUrl("/admin/dashboard");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        // 부모 클래스가 저장된 요청(원래 가려던 URL)으로 리다이렉트 처리
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
