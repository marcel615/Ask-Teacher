package com.github.marcel615.askteacher.domain.auth.controller;

import com.github.marcel615.askteacher.domain.auth.dto.LoginRequest;
import com.github.marcel615.askteacher.domain.auth.dto.LoginResponse;
import com.github.marcel615.askteacher.domain.auth.dto.SignupRequest;
import com.github.marcel615.askteacher.domain.auth.dto.SignupResponse;
import com.github.marcel615.askteacher.domain.auth.service.AuthService;
import com.github.marcel615.askteacher.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SignupResponse> signup(@Valid @RequestBody SignupRequest signupRequest){
        SignupResponse signupResponse = authService.signup(signupRequest);
        return ApiResponse.success(201, "회원가입 성공!", signupResponse);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest){
        LoginResponse loginResponse = authService.login(loginRequest);
        return ApiResponse.success(200, "로그인에 성공했습니다.", loginResponse);
    }

}
