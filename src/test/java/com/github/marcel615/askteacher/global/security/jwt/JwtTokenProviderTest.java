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
                "test-only-q8MzV3RaL5NcH2PtJ7XyK4UwZ9Gf",
                3600000
        );

        @DisplayName("인증 객체는 사용자 ID를 포함한 주체를 사용한다")
        @Test
        void getAuthenticationUsesLongUserIdPrincipal() {
            String token = jwtTokenProvider.createAccessToken(1L, UserRole.USER, "session");

            var verified = jwtTokenProvider.verifyToken(token).orElseThrow();
            Authentication authentication = verified.authentication();

            assertThat(((com.github.marcel615.askteacher.global.security.AskTeacherUserDetails) authentication.getPrincipal()).id()).isEqualTo(1L);
            assertThat(verified.sessionId()).isEqualTo("session");
            assertThat(jwtTokenProvider.validateToken(token)).isTrue();
            assertThat(authentication.getAuthorities()).extracting("authority").containsExactly("USER");
        }

        @DisplayName("잘못된 형식 만료 서명 오류 토큰을 거부한다")
        @Test
        void rejectsMalformedExpiredAndWrongSignatureTokens() {
            assertThat(jwtTokenProvider.validateToken("invalid")).isFalse();
            assertThat(jwtTokenProvider.validateToken("")).isFalse();
            assertThat(jwtTokenProvider.validateToken(null)).isFalse();
            JwtTokenProvider expired = new JwtTokenProvider("test-only-q8MzV3RaL5NcH2PtJ7XyK4UwZ9Gf", -1000);
            assertThat(jwtTokenProvider.validateToken(expired.createAccessToken(1L, UserRole.USER, "session"))).isFalse();
            JwtTokenProvider other = new JwtTokenProvider("other-only-v7KqX2CbM9HsR5JdW4PyT8LzF3An", 3600000);
            assertThat(jwtTokenProvider.validateToken(other.createAccessToken(1L, UserRole.USER, "session"))).isFalse();
        }

        @DisplayName("비어 있거나 충분히 강하지 않은 서명 키로 시작하지 않는다")
        @Test void rejectsMissingOrWeakSigningKey() {
            org.assertj.core.api.Assertions.assertThatThrownBy(() -> new JwtTokenProvider("", 900000)).isInstanceOf(IllegalStateException.class);
            org.assertj.core.api.Assertions.assertThatThrownBy(() -> new JwtTokenProvider("a".repeat(40), 900000)).isInstanceOf(IllegalStateException.class);
            org.assertj.core.api.Assertions.assertThatThrownBy(() -> new JwtTokenProvider("abcdefghij".repeat(4), 900000)).isInstanceOf(IllegalStateException.class);
            org.assertj.core.api.Assertions.assertThatThrownBy(() -> new JwtTokenProvider("abcdefghijklmnop".repeat(2), 900000)).isInstanceOf(IllegalStateException.class);
        }
    }
}
