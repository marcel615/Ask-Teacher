package com.github.marcel615.askteacher.domain.auth.service;

import com.github.marcel615.askteacher.domain.auth.dto.request.SignupRequest;
import com.github.marcel615.askteacher.domain.auth.dto.response.LoginResponse;
import com.github.marcel615.askteacher.domain.auth.dto.response.SignupResponse;
import com.github.marcel615.askteacher.domain.auth.session.RefreshToken;
import com.github.marcel615.askteacher.domain.user.entity.User;
import com.github.marcel615.askteacher.domain.user.repository.UserRepository;
import com.github.marcel615.askteacher.global.exception.CustomException;
import com.github.marcel615.askteacher.global.exception.ErrorCode;
import com.github.marcel615.askteacher.global.security.AskTeacherUserDetails;
import com.github.marcel615.askteacher.global.security.jwt.JwtInformation;
import com.github.marcel615.askteacher.global.security.jwt.JwtRegistry;
import com.github.marcel615.askteacher.global.security.jwt.JwtTokenProvider;
import com.github.marcel615.askteacher.global.security.jwt.VerifiedAccessToken;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.function.Supplier;

@RequiredArgsConstructor
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtRegistry sessionStore;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }
        if (userRepository.existsByNickname(request.nickname())) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        User user = User.createUser(
                request.email(),
                encodedPassword,
                request.nickname()
        );
        User savedUser = userRepository.save(user);
        return SignupResponse.from(savedUser);
    }

    @Transactional(readOnly = true)
    public JwtInformation issueFor(AskTeacherUserDetails principal) {
        RefreshToken refreshToken = executeRedis(() -> sessionStore.login(principal.id()));
        return issueTokens(refreshToken);
    }

    @Transactional(readOnly = true)
    public JwtInformation refresh(String refreshTokenValue) {
        RefreshToken refreshToken = executeRedis(() -> sessionStore.rotate(refreshTokenValue));
        if (refreshToken == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        return issueTokens(refreshToken);
    }

    public void logout(String refreshTokenValue, VerifiedAccessToken verifiedAccessToken) {
        try {
            sessionStore.ensureAvailable();
            if (verifiedAccessToken != null) {
                Authentication authentication = verifiedAccessToken.authentication();
                AskTeacherUserDetails principal = (AskTeacherUserDetails) authentication.getPrincipal();
                sessionStore.logoutAccess(principal.id(), verifiedAccessToken.sessionId());
                return;
            }
            sessionStore.logoutRefresh(refreshTokenValue);
        } catch (DataAccessException exception) {
            throw new RedisUnavailableException(exception);
        }
    }

    private JwtInformation issueTokens(RefreshToken refreshToken) {
        User user = userRepository.findById(refreshToken.userId())
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));

        String accessToken = jwtTokenProvider.createAccessToken(
                user.getId(),
                user.getRole(),
                refreshToken.sessionId()
        );
        LoginResponse response = new LoginResponse(
                accessToken,
                "Bearer",
                Duration.ofMinutes(15).toSeconds()
        );
        return new JwtInformation(response, refreshToken.value());
    }

    private static <T> T executeRedis(Supplier<T> operation) {
        try {
            return operation.get();
        } catch (DataAccessException exception) {
            throw new RedisUnavailableException(exception);
        }
    }
}
