package com.github.marcel615.askteacher.global.security.jwt;

import com.github.marcel615.askteacher.domain.auth.service.RedisUnavailableException;
import com.github.marcel615.askteacher.global.security.SecurityResponseWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtLogoutFilter extends OncePerRequestFilter {
    private final JwtLogoutHandler logoutHandler;
    private final RefreshTokenCookieProvider cookies;
    private final SecurityResponseWriter responses;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return !"POST".equals(request.getMethod()) || !"/api/auth/logout".equals(path);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        try {
            logoutHandler.logout(request, response, authentication);
        } catch (RedisUnavailableException exception) {
            responses.error(response, 503, "Authentication service unavailable");
            return;
        }
        response.addHeader(HttpHeaders.SET_COOKIE, cookies.delete());
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
