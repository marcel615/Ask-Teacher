package com.github.marcel615.askteacher.domain.auth.dto;

import com.github.marcel615.askteacher.domain.user.entity.User;
import lombok.Getter;

@Getter
public class LoginResponse {

    private String accessToken;
    private String tokenType;

    private LoginResponse(String accessToken, String tokenType) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
    }

    public static LoginResponse from(String accessToken, String tokenType) {
        return new LoginResponse(accessToken, tokenType);
    }

}
