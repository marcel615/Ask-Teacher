package com.github.marcel615.askteacher.domain.auth.controller;

import com.github.marcel615.askteacher.domain.auth.dto.*;
import com.github.marcel615.askteacher.domain.auth.service.AuthService;
import com.github.marcel615.askteacher.global.security.jwt.JwtAuthenticationFilter;
import com.github.marcel615.askteacher.global.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final boolean secure;
    private final String sameSite;
    public AuthController(AuthService authService, @Value("${app.auth.cookie-secure:true}") boolean secure,
                          @Value("${app.auth.cookie-same-site:Strict}") String sameSite) {
        this.authService = authService; this.secure = secure; this.sameSite = sameSite;
    }
    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(request));
    }
    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) { return Map.of("token", token.getToken(), "headerName", token.getHeaderName()); }
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) { return response(authService.login(request)); }
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@CookieValue(name = "refreshToken", required = false) String refresh) { return response(authService.refresh(refresh)); }
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = "refreshToken", required = false) String refresh, HttpServletRequest request) {
        var verified = (JwtTokenProvider.Verified) request.getAttribute(JwtAuthenticationFilter.VERIFIED_ATTRIBUTE);
        authService.logout(refresh, verified);
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cookie("", 0)).build();
    }
    private ResponseEntity<LoginResponse> response(AuthService.Issued issued) {
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie(issued.refreshToken(), 7 * 24 * 60 * 60)).body(issued.response());
    }
    private String cookie(String value, long maxAge) {
        return ResponseCookie.from("refreshToken", value).httpOnly(true).secure(secure).sameSite(sameSite)
                .path("/api/auth").maxAge(Duration.ofSeconds(maxAge)).build().toString();
    }
}
