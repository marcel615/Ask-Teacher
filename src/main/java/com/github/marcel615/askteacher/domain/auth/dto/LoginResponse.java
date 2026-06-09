package com.github.marcel615.askteacher.domain.auth.dto;

public record LoginResponse(
        String accessToken,
        String tokenType
) {

    public static LoginResponse from(String accessToken, String tokenType) {
        return new LoginResponse(accessToken, tokenType);
    }
}
