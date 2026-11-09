package com.github.marcel615.askteacher.global.security.jwt;

import org.springframework.security.core.Authentication;

public record VerifiedAccessToken(
        Authentication authentication,
        String sessionId
) {
}
