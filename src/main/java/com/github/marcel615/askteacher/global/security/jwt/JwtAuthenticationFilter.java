package com.github.marcel615.askteacher.global.security.jwt;

import com.github.marcel615.askteacher.domain.auth.session.RedisSessionStore;
import com.github.marcel615.askteacher.global.security.AskTeacherUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    public static final String VERIFIED_ATTRIBUTE = JwtAuthenticationFilter.class.getName() + ".verified";
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisSessionStore sessions;
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return java.util.Set.of("/api/auth/login", "/api/auth/signup", "/api/auth/refresh", "/api/auth/csrf").contains(path);
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            var verified = jwtTokenProvider.verifyToken(token);
            if (verified.isPresent()) {
                var authentication = verified.get().authentication();
                var principal = (AskTeacherUserDetails) authentication.getPrincipal();
                try {
                    if (sessions.active(principal.id(), verified.get().sessionId())) {
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        request.setAttribute(VERIFIED_ATTRIBUTE, verified.get());
                    } else if (!request.getRequestURI().substring(request.getContextPath().length()).equals("/api/auth/logout")) {
                        unauthorized(response); return;
                    }
                } catch (DataAccessException e) {
                    response.setStatus(503); response.setContentType("application/json");
                    response.getWriter().write("{\"status\":503,\"message\":\"Authentication service unavailable\"}");
                    return;
                }
            } else if (!request.getRequestURI().substring(request.getContextPath().length()).equals("/api/auth/logout")) {
                unauthorized(response); return;
            }
        }
        chain.doFilter(request, response);
    }
    private static void unauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(401); response.setContentType("application/json"); response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"status\":401,\"message\":\"인증이 필요합니다.\"}");
    }
}
