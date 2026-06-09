package com.github.marcel615.askteacher.domain.auth.service;

import com.github.marcel615.askteacher.domain.auth.dto.LoginRequest;
import com.github.marcel615.askteacher.domain.auth.dto.LoginResponse;
import com.github.marcel615.askteacher.domain.auth.dto.SignupRequest;
import com.github.marcel615.askteacher.domain.auth.dto.SignupResponse;
import com.github.marcel615.askteacher.domain.user.entity.User;
import com.github.marcel615.askteacher.domain.user.repository.UserRepository;
import com.github.marcel615.askteacher.global.exception.CustomException;
import com.github.marcel615.askteacher.global.exception.ErrorCode;
import com.github.marcel615.askteacher.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public SignupResponse signup(SignupRequest signupRequest) {
        validateDuplicateEmail(signupRequest.email());
        validateDuplicateNickname(signupRequest.nickname());

        String encodedPassword = passwordEncoder.encode(signupRequest.password());

        User user = User.createUser(
                signupRequest.email(),
                encodedPassword,
                signupRequest.nickname()
        );

        User savedUser = userRepository.save(user);

        return SignupResponse.from(savedUser);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_LOGIN_INFO));

        validatePassword(loginRequest.password(), user.getPassword());

        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRole());

        return LoginResponse.from(accessToken, "Bearer");
    }


    private void validateDuplicateEmail(String email) {
        if(userRepository.existsByEmail(email)){
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }
    }

    private void validateDuplicateNickname(String nickname) {
        if(userRepository.existsByNickname(nickname)){
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }
    }

    private void validatePassword(String requestedPassword, String encodedPassword) {
        if (!passwordEncoder.matches(requestedPassword, encodedPassword)) {
            throw new CustomException(ErrorCode.INVALID_LOGIN_INFO);
        }
    }

}
