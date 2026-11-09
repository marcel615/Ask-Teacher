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
    @BeforeEach void createUser() { user = users.saveAndFlush(User.createUser("offline@example.com", passwords.encode("password123"), "offline")); }
    @AfterEach void clean() { users.deleteAllInBatch(); }

    @Nested
    @DisplayName("실제 Redis 연결 장애 시 인증 API")
    class Scenarios {
        @DisplayName("로그인과 재발급은 인증 실패가 아닌 503을 반환한다")
        @Test void loginAndRefreshReturn503() throws Exception {
            var csrf = mvc.perform(get("/api/auth/csrf")).andExpect(status().isOk()).andReturn();
            Cookie csrfCookie = csrf.getResponse().getCookie("XSRF-TOKEN");
            String csrfValue = json.readTree(csrf.getResponse().getContentAsString()).get("token").asText();
            mvc.perform(post("/api/auth/login").cookie(csrfCookie).header("X-XSRF-TOKEN", csrfValue)
                    .contentType("application/json").content("{\"email\":\"offline@example.com\",\"password\":\"password123\"}"))
                    .andExpect(status().isServiceUnavailable());
            Cookie refresh = new Cookie("refreshToken", user.getId() + ".00000000-0000-0000-0000-000000000000." + "a".repeat(43));
            csrf = mvc.perform(get("/api/auth/csrf").cookie(csrfCookie, refresh)).andExpect(status().isOk()).andReturn();
            csrfCookie = csrf.getResponse().getCookie("XSRF-TOKEN");
            csrfValue = json.readTree(csrf.getResponse().getContentAsString()).get("token").asText();
            mvc.perform(post("/api/auth/refresh").cookie(csrfCookie, refresh).header("X-XSRF-TOKEN", csrfValue))
                    .andExpect(status().isServiceUnavailable());
        }
        @DisplayName("인증 요청과 로그아웃은 503이며 CSRF 누락은 403이 우선한다")
        @Test void protectedAndLogoutRespectPriority() throws Exception {
            String access = tokens.createAccessToken(user.getId(), user.getRole(), "session");
            mvc.perform(get("/api/posts").header("Authorization", "Bearer " + access)).andExpect(status().isServiceUnavailable());
            mvc.perform(post("/api/auth/logout")).andExpect(status().isForbidden());
            var csrf = mvc.perform(get("/api/auth/csrf")).andExpect(status().isOk()).andReturn();
            Cookie cookie = csrf.getResponse().getCookie("XSRF-TOKEN");
            String value = json.readTree(csrf.getResponse().getContentAsString()).get("token").asText();
            mvc.perform(post("/api/auth/logout").cookie(cookie).header("X-XSRF-TOKEN", value))
                    .andExpect(status().isServiceUnavailable());
        }
    }
}
