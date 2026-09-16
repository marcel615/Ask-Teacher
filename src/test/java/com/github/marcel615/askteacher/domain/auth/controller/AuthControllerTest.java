package com.github.marcel615.askteacher.domain.auth.controller;

import com.github.marcel615.askteacher.domain.auth.dto.*;
import com.github.marcel615.askteacher.domain.auth.service.AuthService;
import com.github.marcel615.askteacher.global.exception.*;
import com.github.marcel615.askteacher.support.WebTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.util.stream.Stream;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest extends WebTestSupport {
    @MockitoBean AuthService service;

    @Test void signupAndLoginBindJsonAndReturnDirectDtos() throws Exception {
        var signup = new SignupRequest("a@example.com", "12345678", "ab");
        when(service.signup(signup)).thenReturn(new SignupResponse(1L, signup.email(), signup.nickname()));
        mvc.perform(post("/api/auth/signup").contentType("application/json").content("""
                {"email":"a@example.com","password":"12345678","nickname":"ab"}
                """)).andExpect(status().isCreated()).andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value(signup.email())).andExpect(jsonPath("$.nickname").value("ab"))
                .andExpect(jsonPath("$.data").doesNotExist());
        var login = new LoginRequest("a@example.com", "12345678");
        when(service.login(login)).thenReturn(new LoginResponse("token", "Bearer"));
        mvc.perform(post("/api/auth/login").contentType("application/json").content("""
                {"email":"a@example.com","password":"12345678"}
                """)).andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").value("token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
        verify(service).signup(signup);
        verify(service).login(login);
    }

    static Stream<String> invalidSignup() {
        return Stream.of("{}", "{\"email\":\"bad\",\"password\":\"12345678\",\"nickname\":\"ab\"}",
                "{\"email\":\"a@example.com\",\"password\":\"1234567\",\"nickname\":\"ab\"}",
                "{\"email\":\"a@example.com\",\"password\":\"12345678\",\"nickname\":\"a\"}",
                "{\"email\":\"a@example.com\",\"password\":\"12345678\",\"nickname\":\"" + "a".repeat(21) + "\"}",
                "{\"email\":\" \",\"password\":\"        \",\"nickname\":\"  \"}");
    }

    @ParameterizedTest @MethodSource("invalidSignup")
    void signupRejectsInvalidInput(String body) throws Exception {
        mvc.perform(post("/api/auth/signup").contentType("application/json").content(body))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400)).andExpect(jsonPath("$.message").isNotEmpty());
        verifyNoInteractions(service);
    }

    static Stream<String> invalidLogin() {
        return Stream.of("{}", "{\"email\":\"bad\",\"password\":\"password\"}",
                "{\"email\":\" \",\"password\":\"password\"}", "{\"email\":\"a@example.com\",\"password\":\" \"}");
    }

    @ParameterizedTest @MethodSource("invalidLogin")
    void loginRejectsInvalidInput(String body) throws Exception {
        mvc.perform(post("/api/auth/login").contentType("application/json").content(body)).andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test void businessErrorsKeepStatusAndErrorBody() throws Exception {
        when(service.signup(any())).thenThrow(new CustomException(ErrorCode.DUPLICATE_EMAIL));
        mvc.perform(post("/api/auth/signup").contentType("application/json").content("""
                {"email":"a@example.com","password":"12345678","nickname":"ab"}
                """)).andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(ErrorCode.DUPLICATE_EMAIL.getMessage()));
        when(service.login(any())).thenThrow(new CustomException(ErrorCode.INVALID_LOGIN_INFO));
        mvc.perform(post("/api/auth/login").contentType("application/json").content("""
                {"email":"a@example.com","password":"12345678"}
                """)).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401));
    }

    @Test void nicknameMaximumLengthIsAccepted() throws Exception {
        String nickname = "a".repeat(20);
        mvc.perform(post("/api/auth/signup").contentType("application/json")
                .content("{\"email\":\"a@example.com\",\"password\":\"12345678\",\"nickname\":\"" + nickname + "\"}"))
                .andExpect(status().isCreated());
        verify(service).signup(new SignupRequest("a@example.com", "12345678", nickname));
    }
}
