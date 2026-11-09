package com.github.marcel615.askteacher.integration;

import com.github.marcel615.askteacher.domain.auth.session.RedisSessionStore;
import com.github.marcel615.askteacher.domain.auth.session.RefreshToken;
import com.github.marcel615.askteacher.support.ApiTestSupport;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthSessionIntegrationTest extends ApiTestSupport {
    @Autowired
    RedisSessionStore sessions;

    @Nested
    @DisplayName("로그인의 CSRF 검증")
    class LoginCsrf {
        @DisplayName("CSRF 토큰이 없으면 로그인을 거부한다")
        @Test
        void loginRejectsMissingCsrf() throws Exception {
            mvc.perform(post("/api/auth/login")
                            .contentType("application/json")
                            .content("{}"))
                    .andExpect(status().isForbidden());
        }

        @DisplayName("위조된 CSRF 토큰으로 로그인을 거부한다")
        @Test
        void loginRejectsForgedCsrf() throws Exception {
            String forged = "a".repeat(43) + "." + "b".repeat(43);
            mvc.perform(post("/api/auth/login")
                            .cookie(new Cookie("XSRF-TOKEN", forged))
                            .header("X-XSRF-TOKEN", forged)
                            .contentType("application/json")
                            .content("{}"))
                    .andExpect(status().isForbidden());
        }

    }

    @Nested
    @DisplayName("재발급과 재사용 감지")
    class RefreshAndReplay {
        @DisplayName("토큰 회전 후 사용된 토큰을 재사용하면 세션을 폐기한다")
        @Test
        void refreshReplayRevokesAccess() throws Exception {
            Map<String, String> signupBody = Map.of(
                    "email", "session@example.com",
                    "password", "password123",
                    "nickname", "session"
            );
            String signupJson = json.writeValueAsString(signupBody);
            mvc.perform(post("/api/auth/signup")
                            .contentType("application/json")
                            .content(signupJson))
                    .andExpect(status().isCreated());

            MvcResult csrf = mvc.perform(get("/api/auth/csrf"))
                    .andExpect(status().isOk())
                    .andReturn();
            String csrfValue = json.readTree(csrf.getResponse().getContentAsString()).get("token").asText();
            Cookie csrfCookie = csrf.getResponse().getCookie("XSRF-TOKEN");
            assertThat(csrfCookie).isNotNull();

            MvcResult login = mvc.perform(post("/api/auth/login")
                            .cookie(csrfCookie)
                            .header("X-XSRF-TOKEN", csrfValue)
                            .contentType("application/json")
                            .content(json.writeValueAsString(Map.of("email", "session@example.com", "password", "password123"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.expiresIn").value(900))
                    .andReturn();
            Cookie first = login.getResponse().getCookie("refreshToken");
            assertThat(first).isNotNull();
            assertThat(login.getResponse().getHeader("Set-Cookie")).contains("HttpOnly", "SameSite=Strict");

            csrf = mvc.perform(get("/api/auth/csrf")
                            .cookie(csrfCookie, first))
                    .andExpect(status().isOk())
                    .andReturn();
            csrfValue = json.readTree(csrf.getResponse().getContentAsString()).get("token").asText();
            csrfCookie = csrf.getResponse().getCookie("XSRF-TOKEN");
            assertThat(csrfCookie).isNotNull();

            MvcResult refresh = mvc.perform(post("/api/auth/refresh")
                            .cookie(csrfCookie, first)
                            .header("X-XSRF-TOKEN", csrfValue))
                    .andExpect(status().isOk())
                    .andReturn();
            String access = json.readTree(refresh.getResponse().getContentAsString()).get("accessToken").asText();
            assertThat(refresh.getResponse().getCookie("refreshToken").getValue()).isNotEqualTo(first.getValue());

            mvc.perform(post("/api/auth/refresh")
                            .cookie(csrfCookie, first)
                            .header("X-XSRF-TOKEN", csrfValue))
                    .andExpect(status().isUnauthorized());
            mvc.perform(post("/api/posts")
                            .header("Authorization", "Bearer " + access))
                    .andExpect(status().isUnauthorized());
        }

        @DisplayName("동시 회전에서 한 번만 성공하고 재사용으로 세션을 폐기한다")
        @Test
        void concurrentRotationRevokesSession() throws Exception {
            RefreshToken token = sessions.login(910000001L);
            try {
                ExecutorService pool = Executors.newFixedThreadPool(2);
                CountDownLatch start = new CountDownLatch(1);
                try {
                    Callable<RefreshToken> rotate = () -> {
                        start.await();
                        return sessions.rotate(token.value());
                    };
                    Future<RefreshToken> firstRotation = pool.submit(rotate);
                    Future<RefreshToken> secondRotation = pool.submit(rotate);
                    start.countDown();
                    assertThat(Stream.of(firstRotation.get(), secondRotation.get()).filter(Objects::nonNull).count()).isEqualTo(1);
                    assertThat(sessions.active(token.userId(), token.sessionId())).isFalse();
                } finally {
                    pool.shutdownNow();
                }
            } finally {
                redis.delete(sessions.keyForUser(910000001L));
            }
        }

    }

    @Nested
    @DisplayName("세션 교체와 만료")
    class SessionLifetime {
        @DisplayName("새 로그인은 이전 세션을 교체하며 과거 토큰 제출은 새 세션을 유지한다")
        @Test
        void oldSessionCannotRevokeNewLogin() {
            RefreshToken previousToken = sessions.login(910000002L);
            RefreshToken currentToken = sessions.login(910000002L);
            try {
                assertThat(sessions.active(previousToken.userId(), previousToken.sessionId())).isFalse();
                assertThat(sessions.rotate(previousToken.value())).isNull();
                assertThat(sessions.active(currentToken.userId(), currentToken.sessionId())).isTrue();
            } finally {
                redis.delete(sessions.keyForUser(910000002L));
            }
        }

        @DisplayName("JWT 서명 키가 교체되면 이전 Refresh 세션은 재발급할 수 없다")
        @Test
        void signingKeyChangeInvalidatesRefreshSessions() {
            long userId = 910000006L;
            RedisSessionStore replacement = new RedisSessionStore(redis, "other-only-v7KqX2CbM9HsR5JdW4PyT8LzF3An");
            RefreshToken previousToken = sessions.login(userId);
            try {
                assertThat(replacement.rotate(previousToken.value())).isNull();
                assertThat(replacement.active(userId, previousToken.sessionId())).isFalse();
            } finally {
                redis.delete(sessions.keyForUser(userId));
                redis.delete(replacement.keyForUser(userId));
            }
        }

        @DisplayName("절대 만료에 도달하면 회전과 세션 유지가 거부된다")
        @Test
        void absoluteExpirationStopsRotation() {
            RefreshToken token = sessions.login(910000003L);
            try {
                redis.opsForHash().put(sessions.keyForUser(910000003L), "until", "1");
                assertThat(sessions.rotate(token.value())).isNull();
                assertThat(sessions.active(token.userId(), token.sessionId())).isFalse();
            } finally {
                redis.delete(sessions.keyForUser(910000003L));
            }
        }

    }

    @Nested
    @DisplayName("로그아웃과 동시 로그인")
    class Logout {
        @DisplayName("로그아웃은 CSRF를 검증하고 쿠키를 삭제하며 여러 번 호출할 수 있다")
        @Test
        void logoutRequiresCsrfAndClearsCookie() throws Exception {
            mvc.perform(post("/api/auth/logout"))
                    .andExpect(status().isForbidden());
            MvcResult csrf = mvc.perform(get("/api/auth/csrf"))
                    .andExpect(status().isOk())
                    .andReturn();
            String token = json.readTree(csrf.getResponse().getContentAsString()).get("token").asText();
            Cookie cookie = csrf.getResponse().getCookie("XSRF-TOKEN");
            MvcResult result = mvc.perform(post("/api/auth/logout")
                            .cookie(cookie)
                            .header("X-XSRF-TOKEN", token))
                    .andExpect(status().isNoContent())
                    .andReturn();
            assertThat(result.getResponse().getHeader("Set-Cookie")).contains("refreshToken=", "Max-Age=0", "HttpOnly");
            mvc.perform(post("/api/auth/logout")
                            .cookie(cookie)
                            .header("X-XSRF-TOKEN", token))
                    .andExpect(status().isNoContent());
        }

        @DisplayName("Access Token으로 로그아웃하면 두 토큰을 모두 거부한다")
        @Test
        void accessLogoutRevokesBothTokens() throws Exception {
            assertLogoutRevokesBothTokens("logoutaccess", true);
        }

        @DisplayName("Refresh Token으로 로그아웃하면 두 토큰을 모두 거부한다")
        @Test
        void refreshLogoutRevokesBothTokens() throws Exception {
            assertLogoutRevokesBothTokens("logoutrefresh", false);
        }

        private void assertLogoutRevokesBothTokens(String name, boolean useAccessToken) throws Exception {
            Map<String, String> signupBody = Map.of(
                    "email", name + "@example.com",
                    "password", "password123",
                    "nickname", name
            );
            String signupJson = json.writeValueAsString(signupBody);
            mvc.perform(post("/api/auth/signup")
                            .contentType("application/json")
                            .content(signupJson))
                    .andExpect(status().isCreated());
            MvcResult csrf = mvc.perform(get("/api/auth/csrf"))
                    .andExpect(status().isOk())
                    .andReturn();
            Cookie csrfCookie = csrf.getResponse().getCookie("XSRF-TOKEN");
            String csrfValue = json.readTree(csrf.getResponse().getContentAsString()).get("token").asText();
            MvcResult login = mvc.perform(post("/api/auth/login")
                            .cookie(csrfCookie)
                            .header("X-XSRF-TOKEN", csrfValue)
                            .contentType("application/json")
                            .content(json.writeValueAsString(Map.of("email", name + "@example.com", "password", "password123"))))
                    .andExpect(status().isOk())
                    .andReturn();
            Cookie refresh = login.getResponse().getCookie("refreshToken");
            String access = json.readTree(login.getResponse().getContentAsString()).get("accessToken").asText();
            csrf = mvc.perform(get("/api/auth/csrf")
                            .cookie(csrfCookie, refresh))
                    .andExpect(status().isOk())
                    .andReturn();
            csrfCookie = csrf.getResponse().getCookie("XSRF-TOKEN");
            csrfValue = json.readTree(csrf.getResponse().getContentAsString()).get("token").asText();
            MockHttpServletRequestBuilder logout = post("/api/auth/logout")
                    .cookie(csrfCookie, refresh)
                    .header("X-XSRF-TOKEN", csrfValue);
            if (useAccessToken) {
                logout.header("Authorization", "Bearer " + access);
            }
            mvc.perform(logout)
                    .andExpect(status().isNoContent());
            mvc.perform(post("/api/posts")
                            .header("Authorization", "Bearer " + access))
                    .andExpect(status().isUnauthorized());
            mvc.perform(post("/api/auth/refresh")
                            .cookie(csrfCookie, refresh)
                            .header("X-XSRF-TOKEN", csrfValue))
                    .andExpect(status().isUnauthorized());
        }

        @DisplayName("이전 세션의 로그아웃과 새 로그인이 경합해도 새 세션은 유지된다")
        @Test
        void concurrentLoginAndOldLogoutKeepNewSession() throws Exception {
            long userId = 910000005L;
            ExecutorService pool = Executors.newFixedThreadPool(2);
            try {
                for (int attempt = 0; attempt < 20; attempt++) {
                    RefreshToken previousToken = sessions.login(userId);
                    boolean refreshLogout = attempt % 2 == 0;
                    CountDownLatch start = new CountDownLatch(1);
                    Future<RefreshToken> login = pool.submit(() -> {
                        start.await();
                        return sessions.login(userId);
                    });
                    Future<?> logout = pool.submit(() -> {
                        start.await();
                        if (refreshLogout) {
                            sessions.logoutRefresh(previousToken.value());
                        } else {
                            sessions.logoutAccess(userId, previousToken.sessionId());
                        }
                        return null;
                    });
                    start.countDown();
                    RefreshToken currentToken = login.get();
                    logout.get();
                    assertThat(sessions.active(userId, currentToken.sessionId())).isTrue();
                }
            } finally {
                pool.shutdownNow();
                redis.delete(sessions.keyForUser(910000005L));
            }
        }

    }

    @Nested
    @DisplayName("Redis 만료와 연결 장애")
    class RedisBehavior {
        @DisplayName("회전은 7일 유휴 만료와 최초 로그인 기준 절대 만료를 넘지 않는다")
        @Test
        void refreshTtlHonoursBothLimits() {
            RefreshToken token = sessions.login(910000004L);
            String key = sessions.keyForUser(910000004L);
            try {
                assertThat(redis.getExpire(key)).isBetween(604790L, 604800L);
                redis.opsForHash().put(key, "until", Long.toString(System.currentTimeMillis() / 1000 + 20));
                assertThat(sessions.rotate(token.value())).isNotNull();
                assertThat(redis.getExpire(key)).isBetween(1L, 20L);
            } finally {
                redis.delete(key);
            }
        }

        @DisplayName("연결할 수 없는 Redis는 세션을 인증하지 않고 오류를 반환한다")
        @Test
        void unavailableRedisFailsClosed() {
            RedisStandaloneConfiguration config = new RedisStandaloneConfiguration("127.0.0.1", 1);
            LettuceClientConfiguration client = LettuceClientConfiguration.builder()
                    .commandTimeout(Duration.ofSeconds(1)).build();
            LettuceConnectionFactory factory = new LettuceConnectionFactory(config, client);
            factory.afterPropertiesSet();
            factory.start();
            try {
                StringRedisTemplate template = new StringRedisTemplate(factory);
                template.afterPropertiesSet();
                assertThatThrownBy(() -> new RedisSessionStore(template, "test-only-q8MzV3RaL5NcH2PtJ7XyK4UwZ9Gf").active(1L, "session"))
                        .isInstanceOf(DataAccessException.class);
            } finally {
                factory.destroy();
            }
        }
    }
}
