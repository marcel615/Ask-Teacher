package com.github.marcel615.askteacher.global.security.jwt;

import com.github.marcel615.askteacher.domain.auth.session.RedisSessionStore;
import com.github.marcel615.askteacher.domain.auth.session.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisJwtRegistry implements JwtRegistry {
    private final RedisSessionStore sessions;

    @Override
    public RefreshToken login(long userId) {
        return sessions.login(userId);
    }

    @Override
    public RefreshToken rotate(String refreshTokenValue) {
        return sessions.rotate(refreshTokenValue);
    }

    @Override
    public boolean active(long userId, String sessionId) {
        return sessions.active(userId, sessionId);
    }

    @Override
    public void ensureAvailable() {
        sessions.ensureAvailable();
    }

    @Override
    public void logoutAccess(long userId, String sessionId) {
        sessions.logoutAccess(userId, sessionId);
    }

    @Override
    public void logoutRefresh(String refreshTokenValue) {
        sessions.logoutRefresh(refreshTokenValue);
    }
}
