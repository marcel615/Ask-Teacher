package com.github.marcel615.askteacher.global.security.jwt;

import com.github.marcel615.askteacher.domain.auth.session.RedisSessionStore;
import com.github.marcel615.askteacher.global.security.AskTeacherUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    public static final String VERIFIED_ATTRIBUTE = JwtAuthenticationFilter.class.getName() + ".verified";

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisSessionStore sessions;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return Set.of(
                "/api/auth/login",
                "/api/auth/signup",
                "/api/auth/refresh",
                "/api/auth/csrf"
        ).contains(path);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7);
        String requestPath = request.getRequestURI().substring(request.getContextPath().length());
        boolean logoutRequest = "/api/auth/logout".equals(requestPath);
        Optional<VerifiedAccessToken> verifiedToken = jwtTokenProvider.verifyToken(token);
        if (verifiedToken.isEmpty()) {
            if (logoutRequest) {
                filterChain.doFilter(request, response);
            } else {
                unauthorized(response);
            }
            return;
        }

        VerifiedAccessToken verifiedAccessToken = verifiedToken.get();
        Authentication authentication = verifiedAccessToken.authentication();
        AskTeacherUserDetails principal = (AskTeacherUserDetails) authentication.getPrincipal();
        boolean activeSession;
        try {
            activeSession = sessions.active(principal.id(), verifiedAccessToken.sessionId());
        } catch (DataAccessException exception) {
            serviceUnavailable(response);
            return;
        }
        if (!activeSession) {
            if (logoutRequest) {
                filterChain.doFilter(request, response);
            } else {
                unauthorized(response);
            }
            return;
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        request.setAttribute(VERIFIED_ATTRIBUTE, verifiedAccessToken);
        filterChain.doFilter(request, response);
    }

    private static void serviceUnavailable(HttpServletResponse response) throws IOException {
        response.setStatus(503);
        response.setContentType("application/json");
        response.getWriter().write("{\"status\":503,\"message\":\"Authentication service unavailable\"}");
    }

    private static void unauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(401);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"status\":401,\"message\":\"인증이 필요합니다.\"}");
    }
}
