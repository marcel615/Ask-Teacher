package com.github.marcel615.askteacher.global.security.jwt;

import com.github.marcel615.askteacher.domain.auth.session.RefreshToken;

public interface JwtRegistry {
    RefreshToken login(long userId);

    RefreshToken rotate(String refreshTokenValue);

    boolean active(long userId, String sessionId);

    void ensureAvailable();

    void logoutAccess(long userId, String sessionId);

    void logoutRefresh(String refreshTokenValue);
}
