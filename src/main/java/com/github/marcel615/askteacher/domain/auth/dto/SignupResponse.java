package com.github.marcel615.askteacher.domain.auth.dto;

import com.github.marcel615.askteacher.domain.user.entity.User;

public record SignupResponse(
        Long userId,
        String email,
        String nickname
) {

    public static SignupResponse from(User user) {
        return new SignupResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname()
        );
    }
}
