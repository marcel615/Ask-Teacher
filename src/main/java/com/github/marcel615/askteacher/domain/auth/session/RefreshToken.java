package com.github.marcel615.askteacher.domain.auth.session;

public record RefreshToken(
        long userId,
        String sessionId,
        String value
) {
    @Override
    public String toString() {
        return "RefreshToken[userId=" + userId + ", sessionId=" + sessionId + ", value=REDACTED]";
    }
}
