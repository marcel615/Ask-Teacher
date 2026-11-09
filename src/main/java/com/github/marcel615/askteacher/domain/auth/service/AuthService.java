package com.github.marcel615.askteacher.domain.auth.service;

import com.github.marcel615.askteacher.domain.auth.dto.*;
import com.github.marcel615.askteacher.domain.auth.session.RedisSessionStore;
import com.github.marcel615.askteacher.domain.user.entity.User;
import com.github.marcel615.askteacher.domain.user.repository.UserRepository;
import com.github.marcel615.askteacher.global.exception.CustomException;
import com.github.marcel615.askteacher.global.exception.ErrorCode;
import com.github.marcel615.askteacher.global.security.AskTeacherUserDetails;
import com.github.marcel615.askteacher.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final RedisSessionStore sessions;
    public record Issued(LoginResponse response, String refreshToken) {
        @Override public String toString() { return "Issued[response=REDACTED, refreshToken=REDACTED]"; }
    }

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        if (userRepository.existsByNickname(request.nickname())) throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        return SignupResponse.from(userRepository.save(User.createUser(request.email(), passwordEncoder.encode(request.password()), request.nickname())));
    }
    @Transactional(readOnly = true)
    public Issued login(LoginRequest request) {
        try {
            var auth = authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.password()));
            var user = (AskTeacherUserDetails) auth.getPrincipal();
            var refresh = redisCall(() -> sessions.login(user.id()));
            return issue(refresh);
        } catch (AuthenticationException e) { throw new CustomException(ErrorCode.INVALID_LOGIN_INFO); }
    }
    @Transactional(readOnly = true)
    public Issued refresh(String raw) {
        var token = redisCall(() -> sessions.rotate(raw));
        if (token == null) throw new CustomException(ErrorCode.UNAUTHORIZED);
        return issue(token);
    }
    private Issued issue(RedisSessionStore.Token token) {
        User user = userRepository.findById(token.userId()).orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));
        String access = jwtTokenProvider.createAccessToken(user.getId(), user.getRole(), token.sessionId());
        return new Issued(new LoginResponse(access, "Bearer", 900), token.value());
    }
    public void logout(String refresh, JwtTokenProvider.Verified verified) {
        redisCall(() -> { sessions.ensureAvailable(); return null; });
        if (verified != null) {
            var principal = (AskTeacherUserDetails) verified.authentication().getPrincipal();
            redisCall(() -> { sessions.logoutAccess(principal.id(), verified.sessionId()); return null; });
            return;
        }
        redisCall(() -> { sessions.logoutRefresh(refresh); return null; });
    }
    private static <T> T redisCall(java.util.function.Supplier<T> operation) {
        try { return operation.get(); }
        catch (org.springframework.dao.DataAccessException e) { throw new RedisUnavailableException(e); }
    }
}
