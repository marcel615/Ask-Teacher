package com.github.marcel615.askteacher.global.security;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import static org.assertj.core.api.Assertions.*;

class SignedCsrfTokenRepositoryTest {
    @DisplayName("충분한 비밀값이 없으면 시작을 거부한다")
    @Test
    void rejectsShortSecret() {
        assertThatThrownBy(() -> new SignedCsrfTokenRepository("short", true, "Strict"))
                .isInstanceOf(IllegalStateException.class);
    }
    @DisplayName("서명된 토큰만 읽고 Secure HttpOnly 쿠키로 발급한다")
    @Test
    void verifiesSignatureAndSetsSecureCookie() {
        SignedCsrfTokenRepository repository = new SignedCsrfTokenRepository("test-only-csrf-secret-at-least-32-bytes", true, "Strict");
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        CsrfToken token = repository.generateToken(request);
        repository.saveToken(token, request, response);
        assertThat(response.getHeader("Set-Cookie")).contains("Secure", "HttpOnly");
        assertThat(response.getCookie("__Host-XSRF-TOKEN").getAttribute("SameSite")).isEqualTo("Strict");
        request.setCookies(new Cookie("__Host-XSRF-TOKEN", token.getToken()));
        assertThat(repository.loadToken(request).getToken()).isEqualTo(token.getToken());
        request.setCookies(new Cookie("__Host-XSRF-TOKEN", token.getToken()), new Cookie("refreshToken", "other-session"));
        assertThat(repository.loadToken(request)).isNull();
        request.setCookies(new Cookie("__Host-XSRF-TOKEN", "a".repeat(43) + "." + "b".repeat(43)));
        assertThat(repository.loadToken(request)).isNull();
    }
}
