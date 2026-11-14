package com.github.marcel615.askteacher.global.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

@RequiredArgsConstructor
public class AskTeacherAccessDeniedHandler implements AccessDeniedHandler {
    private final SecurityResponseWriter responses;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exception
    ) throws IOException {
        responses.error(response, 403, "접근이 거부되었습니다.");
    }
}
