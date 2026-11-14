package com.github.marcel615.askteacher.global.security.jwt;

import com.github.marcel615.askteacher.global.security.AskTeacherUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {
    @Nested
    @DisplayName("유효한 세션의 인증")
    class ActiveSession {
        @DisplayName("검증 결과를 한 번만 읽어 만료 경계의 재파싱을 피한다")
        @Test
        void usesSingleVerifiedResult() throws Exception {
            JwtTokenProvider provider = mock(JwtTokenProvider.class);
            JwtRegistry sessions = mock(JwtRegistry.class);
            AskTeacherUserDetails principal = new AskTeacherUserDetails(7L, "", "", "USER");
            UsernamePasswordAuthenticationToken authentication = UsernamePasswordAuthenticationToken.authenticated(
                    principal,
                    null,
                    principal.getAuthorities()
            );
            VerifiedAccessToken verifiedAccessToken = new VerifiedAccessToken(authentication, "sid");
            when(provider.verifyToken("token")).thenReturn(Optional.of(verifiedAccessToken));
            when(sessions.active(7L, "sid")).thenReturn(true);
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/posts");
            request.addHeader("Authorization", "Bearer token");
            JwtAuthenticationFilter filter = new JwtAuthenticationFilter(provider, sessions);
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain filterChain = new MockFilterChain();

            try {
                filter.doFilter(request, response, filterChain);


                assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(authentication);
                verify(provider, times(1)).verifyToken("token");
                verifyNoMoreInteractions(provider);
            } finally {
                SecurityContextHolder.clearContext();
            }
        }
    }

    @Nested
    @DisplayName("유효하지 않은 토큰의 거부")
    class InvalidToken {
        @DisplayName("만료된 토큰은 예외 없이 401을 반환한다")
        @Test
        void expiredTokenIsUnauthorized() throws Exception {
            JwtTokenProvider provider = mock(JwtTokenProvider.class);
            when(provider.verifyToken("expired")).thenReturn(Optional.empty());
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/posts");
            request.addHeader("Authorization", "Bearer expired");
            MockHttpServletResponse response = new MockHttpServletResponse();
            JwtRegistry sessionStore = mock(JwtRegistry.class);
            JwtAuthenticationFilter filter = new JwtAuthenticationFilter(provider, sessionStore);
            MockFilterChain filterChain = new MockFilterChain();

            filter.doFilter(request, response, filterChain);

            assertThat(response.getStatus()).isEqualTo(401);
            verify(provider, times(1)).verifyToken("expired");
            verifyNoMoreInteractions(provider);
        }
    }
}
