package com.github.marcel615.askteacher.global.security.jwt;

import com.github.marcel615.askteacher.domain.user.type.UserRole;
import com.github.marcel615.askteacher.global.security.AskTeacherUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {
    private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(
            "test-only-q8MzV3RaL5NcH2PtJ7XyK4UwZ9Gf",
            Duration.ofHours(1)
    );

    @Nested
    @DisplayName("Access Token 검증")
    class AccessTokenVerification {
        @DisplayName("인증 객체는 사용자 ID를 포함한 주체를 사용한다")
        @Test
        void getAuthenticationUsesLongUserIdPrincipal() {
            String token = jwtTokenProvider.createAccessToken(1L, UserRole.USER, "session");

            VerifiedAccessToken verified = jwtTokenProvider.verifyToken(token).orElseThrow();
            Authentication authentication = verified.authentication();

            assertThat(((AskTeacherUserDetails) authentication.getPrincipal()).id()).isEqualTo(1L);
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
            JwtTokenProvider expiredTokenProvider = new JwtTokenProvider(
                    "test-only-q8MzV3RaL5NcH2PtJ7XyK4UwZ9Gf",
                    Duration.ofSeconds(-1)
            );
            String expiredToken = expiredTokenProvider.createAccessToken(1L, UserRole.USER, "session");
            assertThat(jwtTokenProvider.validateToken(expiredToken)).isFalse();
            JwtTokenProvider otherTokenProvider = new JwtTokenProvider(
                    "other-only-v7KqX2CbM9HsR5JdW4PyT8LzF3An",
                    Duration.ofHours(1)
            );
            String incorrectlySignedToken = otherTokenProvider.createAccessToken(1L, UserRole.USER, "session");
            assertThat(jwtTokenProvider.validateToken(incorrectlySignedToken)).isFalse();
        }
    }

    @Nested
    @DisplayName("서명 키 검증")
    class SigningKeyValidation {
        @DisplayName("비어 있거나 충분히 강하지 않은 서명 키로 시작하지 않는다")
        @Test
        void rejectsMissingOrWeakSigningKey() {
            assertThatThrownBy(() -> new JwtTokenProvider(
                    "",
                    Duration.ofMinutes(15)
            ))
                    .isInstanceOf(IllegalStateException.class);
            assertThatThrownBy(() -> new JwtTokenProvider(
                    "a".repeat(40),
                    Duration.ofMinutes(15)
            ))
                    .isInstanceOf(IllegalStateException.class);
            assertThatThrownBy(() -> new JwtTokenProvider(
                    "abcdefghij".repeat(4),
                    Duration.ofMinutes(15)
            ))
                    .isInstanceOf(IllegalStateException.class);
            assertThatThrownBy(() -> new JwtTokenProvider(
                    "abcdefghijklmnop".repeat(2),
                    Duration.ofMinutes(15)
            ))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
