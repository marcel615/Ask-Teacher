package com.github.marcel615.askteacher.domain.auth.dto;

import com.github.marcel615.askteacher.domain.user.entity.User;
import lombok.Getter;

@Getter
public class SignupResponse {

    private final Long userId;
    private final String email;
    private final String nickname;

    private SignupResponse(Long userId, String email, String nickname) {
        this.userId = userId;
        this.email = email;
        this.nickname = nickname;
    }

    public static SignupResponse from(User user) {
        return new SignupResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname()
        );
    }


}
