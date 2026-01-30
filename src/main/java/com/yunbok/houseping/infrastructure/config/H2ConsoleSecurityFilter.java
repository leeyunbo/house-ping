package com.yunbok.houseping.infrastructure.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * H2 Console은 localhost에서만 접근 가능하도록 제한
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class H2ConsoleSecurityFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestUri = request.getRequestURI();

        if (requestUri.startsWith("/h2-console")) {
            String host = request.getHeader("Host");

            // localhost 또는 127.0.0.1에서만 접근 허용
            if (host != null && !host.startsWith("localhost") && !host.startsWith("127.0.0.1")) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "H2 Console is only accessible from localhost");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
