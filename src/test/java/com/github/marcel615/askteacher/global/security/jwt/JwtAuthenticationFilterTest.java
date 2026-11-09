package com.github.marcel615.askteacher.global.security.jwt;

import com.github.marcel615.askteacher.domain.auth.session.RedisSessionStore;
import com.github.marcel615.askteacher.global.security.AskTeacherUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {
    @DisplayName("검증 결과를 한 번만 읽어 만료 경계의 재파싱을 피한다")
    @Test
    void usesSingleVerifiedResult() throws Exception {
        JwtTokenProvider provider = mock(JwtTokenProvider.class);
        RedisSessionStore sessions = mock(RedisSessionStore.class);
        AskTeacherUserDetails principal = new AskTeacherUserDetails(7L, "", "", "USER");
        UsernamePasswordAuthenticationToken auth = UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities());
        when(provider.verifyToken("token")).thenReturn(Optional.of(new JwtTokenProvider.Verified(auth, "sid")));
        when(sessions.active(7L, "sid")).thenReturn(true);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/posts");
        request.addHeader("Authorization", "Bearer token");
        try {
            new JwtAuthenticationFilter(provider, sessions).doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(auth);
            verify(provider, times(1)).verifyToken("token");
            verifyNoMoreInteractions(provider);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
    @DisplayName("만료된 토큰은 예외 없이 401을 반환한다")
    @Test
    void expiredTokenIsUnauthorized() throws Exception {
        JwtTokenProvider provider = mock(JwtTokenProvider.class);
        when(provider.verifyToken("expired")).thenReturn(Optional.empty());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/posts");
        request.addHeader("Authorization", "Bearer expired");
        MockHttpServletResponse response = new MockHttpServletResponse();
        new JwtAuthenticationFilter(provider, mock(RedisSessionStore.class)).doFilter(request, response, new MockFilterChain());
        assertThat(response.getStatus()).isEqualTo(401);
        verify(provider, times(1)).verifyToken("expired");
        verifyNoMoreInteractions(provider);
    }
}
