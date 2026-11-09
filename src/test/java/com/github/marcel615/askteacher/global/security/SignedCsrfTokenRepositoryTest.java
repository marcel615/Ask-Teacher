package com.github.marcel615.askteacher.global.security;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SignedCsrfTokenRepositoryTest {
    private final SignedCsrfTokenRepository repository = new SignedCsrfTokenRepository(
            "test-only-csrf-secret-at-least-32-bytes",
            true,
            "Strict"
    );

    @Nested
    @DisplayName("서명 키 검증")
    class SigningKey {
        @DisplayName("충분한 비밀값이 없으면 시작을 거부한다")
        @Test
        void rejectsShortSecret() {
            assertThatThrownBy(() -> new SignedCsrfTokenRepository("short", true, "Strict"))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("CSRF 쿠키 발급")
    class CookieIssuance {
        @DisplayName("Secure HttpOnly 쿠키와 SameSite 속성을 적용한다")
        @Test
        void setsSecureCookieAttributes() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            CsrfToken token = repository.generateToken(request);

            repository.saveToken(token, request, response);

            Cookie csrfCookie = response.getCookie("__Host-XSRF-TOKEN");
            assertThat(response.getHeader("Set-Cookie")).contains("Secure", "HttpOnly");
            assertThat(csrfCookie.getAttribute("SameSite")).isEqualTo("Strict");
        }
    }

    @Nested
    @DisplayName("CSRF 서명 검증")
    class SignatureVerification {
        @DisplayName("서명이 유효한 쿠키의 토큰을 읽는다")
        @Test
        void loadsSignedToken() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            CsrfToken token = repository.generateToken(request);
            request.setCookies(new Cookie("__Host-XSRF-TOKEN", token.getToken()));

            CsrfToken loadedToken = repository.loadToken(request);

            assertThat(loadedToken.getToken()).isEqualTo(token.getToken());
        }

        @DisplayName("Refresh 쿠키가 바뀌면 이전 CSRF 토큰을 거부한다")
        @Test
        void rejectsTokenFromDifferentRefreshCookie() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            CsrfToken token = repository.generateToken(request);
            request.setCookies(
                    new Cookie("__Host-XSRF-TOKEN", token.getToken()),
                    new Cookie("refreshToken", "other-session")
            );

            assertThat(repository.loadToken(request)).isNull();
        }

        @DisplayName("위조된 서명의 CSRF 쿠키를 거부한다")
        @Test
        void rejectsForgedSignature() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            String forgedToken = "a".repeat(43) + "." + "b".repeat(43);
            request.setCookies(new Cookie("__Host-XSRF-TOKEN", forgedToken));

            assertThat(repository.loadToken(request)).isNull();
        }
    }
}
