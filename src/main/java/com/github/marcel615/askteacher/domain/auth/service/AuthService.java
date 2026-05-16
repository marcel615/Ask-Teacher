package com.github.marcel615.askteacher.domain.auth.service;

import com.github.marcel615.askteacher.domain.auth.dto.SignupRequest;
import com.github.marcel615.askteacher.domain.auth.dto.SignupResponse;
import com.github.marcel615.askteacher.domain.user.entity.User;
import com.github.marcel615.askteacher.domain.user.repository.UserRepository;
import com.github.marcel615.askteacher.global.exception.CustomException;
import com.github.marcel615.askteacher.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignupResponse signup(SignupRequest signupRequest) {
        validateDuplicateEmail(signupRequest.getEmail());
        validateDuplicateNickname(signupRequest.getNickname());

        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());

        User user = User.createUser(
                signupRequest.getEmail(),
                encodedPassword,
                signupRequest.getNickname()
        );

        User savedUser = userRepository.save(user);

        return SignupResponse.from(savedUser);
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

}
