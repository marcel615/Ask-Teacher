package com.github.marcel615.askteacher.global.security.jwt;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RefreshTokenCookieProvider {
    private final boolean secure;
    private final String sameSite;

    public RefreshTokenCookieProvider(
            @Value("${app.auth.cookie-secure:true}") boolean secure,
            @Value("${app.auth.cookie-same-site:Strict}") String sameSite
    ) {
        this.secure = secure;
        this.sameSite = sameSite;
    }

    public String create(String refreshToken) {
        return cookie(refreshToken, Duration.ofDays(7));
    }

    public String delete() {
        return cookie("", Duration.ZERO);
    }

    public String read(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private String cookie(String value, Duration maxAge) {
        return ResponseCookie.from("refreshToken", value)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/api/auth")
                .maxAge(maxAge)
                .build()
                .toString();
    }
}
