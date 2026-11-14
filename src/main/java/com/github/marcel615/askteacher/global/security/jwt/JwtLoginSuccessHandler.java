package com.github.marcel615.askteacher.global.security.jwt;

import com.github.marcel615.askteacher.domain.auth.service.AuthService;
import com.github.marcel615.askteacher.global.security.AskTeacherUserDetails;
import com.github.marcel615.askteacher.global.security.SecurityResponseWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {
    private final AuthService authService;
    private final RefreshTokenCookieProvider cookies;
    private final SecurityResponseWriter responses;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        AskTeacherUserDetails principal = (AskTeacherUserDetails) authentication.getPrincipal();
        JwtInformation information = authService.issueFor(principal);
        response.addHeader(HttpHeaders.SET_COOKIE, cookies.create(information.refreshToken()));
        responses.json(response, 200, information.response());
    }
}
