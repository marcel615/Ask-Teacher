package com.github.marcel615.askteacher.integration;

import com.github.marcel615.askteacher.domain.user.entity.User;
import com.github.marcel615.askteacher.domain.user.repository.UserRepository;
import com.github.marcel615.askteacher.global.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"spring.data.redis.host=127.0.0.1", "spring.data.redis.port=1", "spring.data.redis.timeout=1s"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AuthRedisFailureIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder passwords;
    @Autowired JwtTokenProvider tokens;
    User user;
    @BeforeEach
    void createUser() {
        user = users.saveAndFlush(User.createUser("offline@example.com", passwords.encode("password123"), "offline"));
    }

    @AfterEach
    void clean() {
        users.deleteAllInBatch();
    }

    @Nested
    @DisplayName("Redis 연결 장애 시 토큰 발급")
    class TokenIssuance {
        @DisplayName("Redis 연결 장애 시 로그인은 503을 반환한다")
        @Test
        void loginReturns503() throws Exception {
            MvcResult csrf = mvc.perform(get("/api/auth/csrf")).andExpect(status().isOk()).andReturn();
            Cookie csrfCookie = csrf.getResponse().getCookie("XSRF-TOKEN");
            String csrfValue = json.readTree(csrf.getResponse().getContentAsString()).get("token").asText();
            mvc.perform(post("/api/auth/login").cookie(csrfCookie).header("X-XSRF-TOKEN", csrfValue)
                    .contentType("application/json").content("{\"email\":\"offline@example.com\",\"password\":\"password123\"}"))
                    .andExpect(status().isServiceUnavailable());
        }

        @DisplayName("Redis 연결 장애 시 재발급은 503을 반환한다")
        @Test
        void refreshReturns503() throws Exception {
            Cookie refresh = new Cookie("refreshToken", user.getId() + ".00000000-0000-0000-0000-000000000000." + "a".repeat(43));
            MvcResult csrf = mvc.perform(get("/api/auth/csrf").cookie(refresh)).andExpect(status().isOk()).andReturn();
            Cookie csrfCookie = csrf.getResponse().getCookie("XSRF-TOKEN");
            String csrfValue = json.readTree(csrf.getResponse().getContentAsString()).get("token").asText();
            mvc.perform(post("/api/auth/refresh").cookie(csrfCookie, refresh).header("X-XSRF-TOKEN", csrfValue))
                    .andExpect(status().isServiceUnavailable());
        }
    }

    @Nested
    @DisplayName("Redis 연결 장애 시 인증 요청과 로그아웃")
    class AuthenticatedRequests {
        @DisplayName("Redis 연결 장애 시 인증 요청은 503을 반환한다")
        @Test
        void authenticatedRequestReturns503() throws Exception {
            String access = tokens.createAccessToken(user.getId(), user.getRole(), "session");
            mvc.perform(get("/api/posts").header("Authorization", "Bearer " + access))
                    .andExpect(status().isServiceUnavailable());
        }

        @DisplayName("Redis 연결 장애 시 CSRF 없는 로그아웃은 403을 우선 반환한다")
        @Test
        void logoutWithoutCsrfReturns403() throws Exception {
            mvc.perform(post("/api/auth/logout")).andExpect(status().isForbidden());
        }

        @DisplayName("Redis 연결 장애 시 유효한 CSRF로 로그아웃하면 503을 반환한다")
        @Test
        void logoutWithCsrfReturns503() throws Exception {
            MvcResult csrf = mvc.perform(get("/api/auth/csrf")).andExpect(status().isOk()).andReturn();
            Cookie cookie = csrf.getResponse().getCookie("XSRF-TOKEN");
            String value = json.readTree(csrf.getResponse().getContentAsString()).get("token").asText();
            mvc.perform(post("/api/auth/logout").cookie(cookie).header("X-XSRF-TOKEN", value))
                    .andExpect(status().isServiceUnavailable());
        }
    }
}