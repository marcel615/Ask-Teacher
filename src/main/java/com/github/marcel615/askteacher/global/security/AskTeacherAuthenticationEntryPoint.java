package com.github.marcel615.askteacher.global.security;

import com.github.marcel615.askteacher.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

@RequiredArgsConstructor
public class AskTeacherAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final SecurityResponseWriter responses;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {
        responses.error(response, ErrorCode.UNAUTHORIZED);
    }
}
