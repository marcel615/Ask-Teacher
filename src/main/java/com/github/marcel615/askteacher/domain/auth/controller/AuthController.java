package com.github.marcel615.askteacher.domain.auth.controller;

import com.github.marcel615.askteacher.domain.auth.dto.request.SignupRequest;
import com.github.marcel615.askteacher.domain.auth.dto.response.CsrfTokenResponse;
import com.github.marcel615.askteacher.domain.auth.dto.response.LoginResponse;
import com.github.marcel615.askteacher.domain.auth.dto.response.SignupResponse;
import com.github.marcel615.askteacher.domain.auth.service.AuthService;
import com.github.marcel615.askteacher.global.security.jwt.JwtInformation;
import com.github.marcel615.askteacher.global.security.jwt.RefreshTokenCookieProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final RefreshTokenCookieProvider cookies;

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/csrf")
    public CsrfTokenResponse csrf(CsrfToken token) {
        return new CsrfTokenResponse(token.getToken(), token.getHeaderName());
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(HttpServletRequest request) {
        JwtInformation information = authService.refresh(cookies.read(request));
        String refreshCookie = cookies.create(information.refreshToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie)
                .body(information.response());
    }
}
