package com.github.marcel615.askteacher.global.security.jwt;

import com.github.marcel615.askteacher.domain.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {
    private final AuthService authService;
    private final RefreshTokenCookieProvider cookies;

    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        VerifiedAccessToken verified = (VerifiedAccessToken) request.getAttribute(
                JwtAuthenticationFilter.VERIFIED_ATTRIBUTE
        );
        authService.logout(cookies.read(request), verified);
    }
}
