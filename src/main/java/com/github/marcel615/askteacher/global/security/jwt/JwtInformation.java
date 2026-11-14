package com.github.marcel615.askteacher.global.security.jwt;

import com.github.marcel615.askteacher.domain.auth.dto.response.LoginResponse;

public record JwtInformation(
        LoginResponse response,
        String refreshToken
) {
    @Override
    public String toString() {
        return "JwtInformation[response=REDACTED, refreshToken=REDACTED]";
    }
}
