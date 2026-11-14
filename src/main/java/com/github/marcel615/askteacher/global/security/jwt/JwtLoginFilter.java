package com.github.marcel615.askteacher.global.security.jwt;

import com.github.marcel615.askteacher.domain.auth.dto.request.LoginRequest;
import com.github.marcel615.askteacher.domain.auth.service.RedisUnavailableException;
import com.github.marcel615.askteacher.global.exception.CustomException;
import com.github.marcel615.askteacher.global.exception.ErrorCode;
import com.github.marcel615.askteacher.global.security.SecurityResponseWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@RequiredArgsConstructor
public class JwtLoginFilter extends OncePerRequestFilter {
    private final AuthenticationManager authenticationManager;
    private final JacksonJsonHttpMessageConverter jsonConverter;
    private final Validator validator;
    private final JwtLoginSuccessHandler successHandler;
    private final JwtLoginFailureHandler failureHandler;
    private final SecurityResponseWriter responses;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return !"POST".equals(request.getMethod()) || !"/api/auth/login".equals(path);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        LoginRequest login;
        try {
            ServletServerHttpRequest input = new ServletServerHttpRequest(request);
            MediaType contentType = input.getHeaders().getContentType();
            MediaType inputType = contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM;
            if (!jsonConverter.canRead(LoginRequest.class, inputType)) {
                // MVC treats an absent type with no body as a missing required body.
                if (contentType == null && input.getBody().read() == -1) {
                    responses.error(response, ErrorCode.INVALID_INPUT_VALUE);
                } else {
                    // Preserve the existing advice's response to unsupported media types.
                    responses.error(response, ErrorCode.INTERNAL_SERVER_ERROR);
                }
                return;
            }
            login = (LoginRequest) jsonConverter.read(LoginRequest.class, input);
        } catch (InvalidMediaTypeException exception) {
            responses.error(response, ErrorCode.INTERNAL_SERVER_ERROR);
            return;
        } catch (HttpMessageNotReadableException | IOException exception) {
            responses.error(response, ErrorCode.INVALID_INPUT_VALUE);
            return;
        }
        if (login == null) {
            responses.error(response, ErrorCode.INVALID_INPUT_VALUE);
            return;
        }

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(login);
        if (!violations.isEmpty()) {
            responses.error(response, 400, violations.iterator().next().getMessage());
            return;
        }

        UsernamePasswordAuthenticationToken credentials = UsernamePasswordAuthenticationToken.unauthenticated(
                login.email(),
                login.password()
        );
        try {
            Authentication authentication = authenticationManager.authenticate(credentials);
            successHandler.onAuthenticationSuccess(request, response, authentication);
        } catch (AuthenticationException exception) {
            failureHandler.onAuthenticationFailure(request, response, exception);
        } catch (RedisUnavailableException exception) {
            responses.error(response, 503, "Authentication service unavailable");
        } catch (CustomException exception) {
            responses.error(response, exception.getErrorCode());
        }
    }
}
