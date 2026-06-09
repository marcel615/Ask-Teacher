package com.github.marcel615.askteacher.global.security.jwt;

import com.github.marcel615.askteacher.domain.user.type.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(
            "my-secret-key-my-secret-key-my-secret-key",
            3600000
    );

    @Test
    void getAuthenticationUsesLongUserIdPrincipal() {
        String token = jwtTokenProvider.createAccessToken(1L, UserRole.USER);

        Authentication authentication = jwtTokenProvider.getAuthentication(token);

        assertThat(authentication.getPrincipal()).isEqualTo(1L);
        assertThat(authentication.getPrincipal()).isInstanceOf(Long.class);
    }
}
