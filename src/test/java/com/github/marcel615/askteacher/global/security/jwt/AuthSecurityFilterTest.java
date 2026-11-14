package com.github.marcel615.askteacher.global.security.jwt;

import com.github.marcel615.askteacher.domain.auth.controller.AuthController;
import com.github.marcel615.askteacher.domain.auth.dto.response.LoginResponse;
import com.github.marcel615.askteacher.domain.auth.service.RedisUnavailableException;
import com.github.marcel615.askteacher.global.security.AskTeacherUserDetails;
import com.github.marcel615.askteacher.support.WebTestSupport;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthSecurityFilterTest extends WebTestSupport {
    @MockitoBean
    AuthenticationManager authenticationManager;

    @Nested
    @DisplayName("로그인 요청과 응답")
    class Login {
        @DisplayName("JSON 자격을 인증하고 Access 응답과 보호된 Refresh 쿠키를 발급한다")
        @ParameterizedTest
        @ValueSource(strings = {"application/json", "application/vnd.askteacher+json"})
        void loginBindsJsonAndReturnsResponse(String contentType) throws Exception {
            AskTeacherUserDetails principal = new AskTeacherUserDetails(7L, "a@example.com", "encoded", "USER");
            UsernamePasswordAuthenticationToken authentication = UsernamePasswordAuthenticationToken.authenticated(
                    principal,
                    null,
                    principal.getAuthorities()
            );
            when(authenticationManager.authenticate(any())).thenReturn(authentication);
            when(service.issueFor(principal)).thenReturn(new JwtInformation(
                    new LoginResponse("token", "Bearer", 900),
                    "refresh"
            ));

            mvc.perform(post("/api/auth/login")
                            .with(csrf())
                            .contentType(contentType)
                            .content("{\"email\":\"a@example.com\",\"password\":\"12345678\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("token"))
                    .andExpect(jsonPath("$.tokenType").value("Bearer"))
                    .andExpect(jsonPath("$.expiresIn").value(900))
                    .andExpect(jsonPath("$.refreshToken").doesNotExist())
                    .andExpect(header().string("Set-Cookie", containsString("refreshToken=refresh")))
                    .andExpect(cookie().path("refreshToken", "/api/auth"))
                    .andExpect(cookie().httpOnly("refreshToken", true))
                    .andExpect(cookie().maxAge("refreshToken", 604800))
                    .andExpect(header().string("Set-Cookie", containsString("SameSite=Strict")))
                    .andExpect(cookie().doesNotExist("JSESSIONID"));

            verify(authenticationManager).authenticate(argThat(credentials ->
                    "a@example.com".equals(credentials.getPrincipal())
                            && "12345678".equals(credentials.getCredentials())));
            verify(service).issueFor(principal);
        }

        @DisplayName("미지원 Content-Type은 본문 유무에 관계없이 기존 MVC 오류 응답을 유지한다")
        @ParameterizedTest
        @CsvSource({"text/plain,true", "application/xml,false"})
        void unsupportedContentTypeKeepsExistingError(String contentType, boolean hasBody) throws Exception {
            String body = hasBody ? "{\"email\":\"a@example.com\",\"password\":\"password\"}" : "";

            mvc.perform(post("/api/auth/login").with(csrf()).contentType(contentType).content(body))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다."));

            verifyNoInteractions(authenticationManager, service);
        }

        @DisplayName("토큰 발급 중 DB 장애는 500 JSON을 반환하고 Refresh 쿠키를 발급하지 않는다")
        @Test
        void issuanceDatabaseFailureReturns500WithoutRefreshCookie() throws Exception {
            AskTeacherUserDetails principal = new AskTeacherUserDetails(7L, "a@example.com", "encoded", "USER");
            UsernamePasswordAuthenticationToken authentication = UsernamePasswordAuthenticationToken.authenticated(
                    principal,
                    null,
                    principal.getAuthorities()
            );
            when(authenticationManager.authenticate(any())).thenReturn(authentication);
            when(service.issueFor(principal)).thenThrow(new DataAccessResourceFailureException("unavailable"));

            mvc.perform(post("/api/auth/login")
                            .with(csrf())
                            .contentType("application/json")
                            .content("{\"email\":\"a@example.com\",\"password\":\"password\"}"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().contentTypeCompatibleWith("application/json"))
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다."))
                    .andExpect(cookie().doesNotExist("refreshToken"));
        }

        @DisplayName("Content-Type 없이 JSON 본문을 보내면 기존 MVC 미지원 타입 오류를 유지한다")
        @Test
        void missingContentTypeWithBodyIsRejected() throws Exception {
            mvc.perform(post("/api/auth/login")
                            .with(csrf())
                            .content("{\"email\":\"a@example.com\",\"password\":\"password\"}"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500));

            verifyNoInteractions(authenticationManager, service);
        }

        @DisplayName("Content-Type과 본문이 모두 없으면 필수 본문 오류로 거부한다")
        @Test
        void missingContentTypeAndBodyReturns400() throws Exception {
            mvc.perform(post("/api/auth/login").with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다."));

            verifyNoInteractions(authenticationManager, service);
        }

        @DisplayName("필수값 누락과 잘못된 이메일 또는 빈 비밀번호를 거부한다")
        @ParameterizedTest
        @ValueSource(strings = {
                "{}",
                "{\"email\":\"bad\",\"password\":\"password\"}",
                "{\"email\":\" \",\"password\":\"password\"}",
                "{\"email\":\"a@example.com\",\"password\":\" \"}"
        })
        void loginRejectsInvalidInput(String body) throws Exception {
            mvc.perform(post("/api/auth/login")
                            .with(csrf())
                            .contentType("application/json")
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").isNotEmpty());

            verifyNoInteractions(authenticationManager, service);
        }

        @DisplayName("잘못된 이메일과 빈 비밀번호의 기존 검증 메시지를 유지한다")
        @ParameterizedTest
        @CsvSource({"bad,password,올바른 이메일 형식이 아닙니다.", "a@example.com,' ',비밀번호는 필수입니다."})
        void loginKeepsValidationMessages(String email, String password, String message) throws Exception {
            String body = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";

            mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json").content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(message));

            verifyNoInteractions(authenticationManager, service);
        }

        @DisplayName("깨진 JSON과 빈 본문 또는 null은 입력 오류로 반환한다")
        @ParameterizedTest
        @ValueSource(strings = {"{", "", "null"})
        void malformedJsonReturns400(String body) throws Exception {
            mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json").content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다."));

            verifyNoInteractions(authenticationManager, service);
        }

        @DisplayName("이메일이 없거나 비밀번호가 틀리면 401로 반환하고 토큰을 발급하지 않는다")
        @Test
        void badCredentialsReturn401() throws Exception {
            when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

            mvc.perform(post("/api/auth/login")
                            .with(csrf())
                            .contentType("application/json")
                            .content("{\"email\":\"a@example.com\",\"password\":\"wrong\"}"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 일치하지 않습니다."))
                    .andExpect(cookie().doesNotExist("refreshToken"));

            verifyNoInteractions(service);
        }
    }

    @Nested
    @DisplayName("로그아웃과 필터 실행 순서")
    class Logout {
        @DisplayName("로그아웃 장애는 503을 반환하며 Refresh 쿠키를 삭제하지 않는다")
        @Test
        void logoutStorageFailureReturns503() throws Exception {
            doThrow(new RedisUnavailableException(new RuntimeException()))
                    .when(service).logout("token", null);

            mvc.perform(post("/api/auth/logout").with(csrf()).cookie(new Cookie("refreshToken", "token")))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.status").value(503))
                    .andExpect(jsonPath("$.message").value("Authentication service unavailable"))
                    .andExpect(cookie().doesNotExist("refreshToken"));
        }

        @DisplayName("CSRF 검증은 로그인과 로그아웃 처리보다 먼저 실행된다")
        @ParameterizedTest
        @ValueSource(strings = {"/api/auth/login", "/api/auth/logout"})
        void csrfRunsBeforeAuthHandlers(String path) throws Exception {
            mvc.perform(post(path).contentType("application/json").content("{}"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("접근이 거부되었습니다."));

            verifyNoInteractions(authenticationManager, service);
        }

        @DisplayName("POST가 아닌 요청은 로그인과 로그아웃 핸들러를 호출하지 않는다")
        @ParameterizedTest
        @ValueSource(strings = {"/api/auth/login", "/api/auth/logout"})
        void getDoesNotRunAuthHandlers(String path) throws Exception {
            mvc.perform(get(path))
                    .andExpect(cookie().doesNotExist("refreshToken"))
                    .andExpect(jsonPath("$.accessToken").doesNotExist());

            verifyNoInteractions(authenticationManager, service);
        }
    }
}
