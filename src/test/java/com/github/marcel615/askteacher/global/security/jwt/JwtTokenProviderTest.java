package com.github.marcel615.askteacher.global.security.jwt;

import com.github.marcel615.askteacher.domain.user.type.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {
    @Nested
    @DisplayName("JWT 인증과 검증")
    class Scenarios {

        private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(
                "my-secret-key-my-secret-key-my-secret-key",
                3600000
        );

        @DisplayName("인증 객체는 Long 사용자 ID를 주체로 사용한다")
        @Test
        void getAuthenticationUsesLongUserIdPrincipal() {
            String token = jwtTokenProvider.createAccessToken(1L, UserRole.USER);

            Authentication authentication = jwtTokenProvider.getAuthentication(token);

            assertThat(authentication.getPrincipal()).isEqualTo(1L);
            assertThat(authentication.getPrincipal()).isInstanceOf(Long.class);
            assertThat(jwtTokenProvider.validateToken(token)).isTrue();
            assertThat(authentication.getAuthorities()).extracting("authority").containsExactly("USER");
        }

        @DisplayName("잘못된 형식 만료 서명 오류 토큰을 거부한다")
        @Test
        void rejectsMalformedExpiredAndWrongSignatureTokens() {
            assertThat(jwtTokenProvider.validateToken("invalid")).isFalse();
            assertThat(jwtTokenProvider.validateToken("")).isFalse();
            assertThat(jwtTokenProvider.validateToken(null)).isFalse();
            JwtTokenProvider expired = new JwtTokenProvider("my-secret-key-my-secret-key-my-secret-key", -1000);
            assertThat(jwtTokenProvider.validateToken(expired.createAccessToken(1L, UserRole.USER))).isFalse();
            JwtTokenProvider other = new JwtTokenProvider("other-test-key-other-test-key-other-test-key", 3600000);
            assertThat(jwtTokenProvider.validateToken(other.createAccessToken(1L, UserRole.USER))).isFalse();
        }
    }
}
