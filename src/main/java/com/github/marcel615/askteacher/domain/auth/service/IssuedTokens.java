package com.github.marcel615.askteacher.domain.auth.service;

import com.github.marcel615.askteacher.domain.auth.dto.response.LoginResponse;

public record IssuedTokens(
        LoginResponse response,
        String refreshToken
) {
    @Override
    public String toString() {
        return "IssuedTokens[response=REDACTED, refreshToken=REDACTED]";
    }
}
