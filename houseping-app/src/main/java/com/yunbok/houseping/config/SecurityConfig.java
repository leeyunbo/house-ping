package com.yunbok.houseping.config;

import com.yunbok.houseping.config.oauth2.CustomOAuth2UserService;
import com.yunbok.houseping.config.oauth2.OAuth2AuthenticationFailureHandler;
import com.yunbok.houseping.config.oauth2.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler successHandler;
    private final OAuth2AuthenticationFailureHandler failureHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")
            )
            .authorizeHttpRequests(auth -> auth
                // 메인 페이지 (공개)
                .requestMatchers("/").permitAll()
                .requestMatchers("/home", "/home/**").permitAll()
                .requestMatchers("/favicon.ico", "/favicon.svg", "/robots.txt", "/sitemap.xml", "/*.html").permitAll()
                // 인증 관련
                .requestMatchers("/auth/**", "/oauth/**").permitAll()
                // Actuator: health만 공개, 나머지는 MASTER만
                .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                .requestMatchers("/actuator/**").hasRole("MASTER")
                // 관리자 페이지 (MASTER/ADMIN만)
                .requestMatchers("/admin/users/**").hasRole("MASTER")
                .requestMatchers("/admin/system/**").hasRole("MASTER")
                .requestMatchers("/admin/**").hasAnyRole("MASTER", "ADMIN")
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/auth/login")
                .redirectionEndpoint(redirect -> redirect
                    .baseUri("/oauth/naver/callback")
                )
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                )
                .successHandler(successHandler)
                .failureHandler(failureHandler)
            )
            .logout(logout -> logout
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/auth/login?logout")
                .logoutRequestMatcher(RegexRequestMatcher.regexMatcher("/auth/logout"))
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            );

        return http.build();
    }
}
