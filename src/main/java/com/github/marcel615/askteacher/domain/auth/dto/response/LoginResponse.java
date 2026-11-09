package com.github.marcel615.askteacher.domain.auth.dto.response;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
    @Override
    public String toString() {
        return "LoginResponse[accessToken=REDACTED, tokenType=" + tokenType + ", expiresIn=" + expiresIn + "]";
    }
}
