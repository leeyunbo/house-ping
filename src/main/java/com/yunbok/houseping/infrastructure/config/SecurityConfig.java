package com.yunbok.houseping.infrastructure.config;

import com.yunbok.houseping.infrastructure.security.CustomOAuth2UserService;
import com.yunbok.houseping.infrastructure.security.OAuth2AuthenticationFailureHandler;
import com.yunbok.houseping.infrastructure.security.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

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
                .requestMatchers("/").permitAll()
                .requestMatchers("/home", "/home/**").hasRole("MASTER")
                .requestMatchers("/favicon.ico", "/favicon.svg").permitAll()
                .requestMatchers("/auth/**", "/oauth/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/admin/users/**").hasRole("MASTER")
                .requestMatchers("/admin/system/**").hasRole("MASTER")
                .requestMatchers("/admin/**").authenticated()
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
                .logoutRequestMatcher(new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/auth/logout", "GET"))
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            );

        return http.build();
    }
}
