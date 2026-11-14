package com.github.marcel615.askteacher.global.security;

import com.github.marcel615.askteacher.global.exception.ErrorCode;
import com.github.marcel615.askteacher.global.response.ErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public class SecurityResponseWriter {
    private final ObjectMapper objectMapper;

    public void error(HttpServletResponse response, ErrorCode code) throws IOException {
        error(response, code.getStatus(), code.getMessage());
    }

    public void error(HttpServletResponse response, int status, String message) throws IOException {
        json(response, status, ErrorResponse.of(status, message));
    }

    public void json(HttpServletResponse response, int status, Object body) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), body);
    }
}
