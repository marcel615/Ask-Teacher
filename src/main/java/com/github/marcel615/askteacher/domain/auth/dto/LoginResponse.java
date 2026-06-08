package com.github.marcel615.askteacher.domain.auth.dto;

import com.github.marcel615.askteacher.domain.user.entity.User;
import lombok.Getter;

@Getter
public class LoginResponse {

    private final String nickname;

    private LoginResponse(String nickname) {
        this.nickname = nickname;
    }

    public static LoginResponse from(User user) {
        return new LoginResponse(user.getNickname());
    }

}
