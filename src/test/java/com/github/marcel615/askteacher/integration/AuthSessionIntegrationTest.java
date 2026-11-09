package com.github.marcel615.askteacher.integration;

import com.github.marcel615.askteacher.domain.auth.session.RedisSessionStore;
import com.github.marcel615.askteacher.support.ApiTestSupport;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;
import java.util.Map;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthSessionIntegrationTest extends ApiTestSupport {
    @Autowired RedisSessionStore sessions;

    @Nested
    @DisplayName("실제 Redis를 사용하는 인증 세션")
    class Scenarios {
        @DisplayName("CSRF와 회전을 적용하고 사용된 토큰의 재사용은 세션을 폐기한다")
        @Test void refreshReplayRevokesAccess() throws Exception {
            mvc.perform(post("/api/auth/signup").contentType("application/json").content(json.writeValueAsString(
                    Map.of("email", "session@example.com", "password", "password123", "nickname", "session"))))
                    .andExpect(status().isCreated());
            MvcResult csrf = mvc.perform(get("/api/auth/csrf")).andExpect(status().isOk()).andReturn();
            String csrfValue = json.readTree(csrf.getResponse().getContentAsString()).get("token").asText();
            Cookie csrfCookie = csrf.getResponse().getCookie("XSRF-TOKEN");
            assertThat(csrfCookie).isNotNull();
            mvc.perform(post("/api/auth/login").contentType("application/json").content("{}"))
                    .andExpect(status().isForbidden());
            String forged = "a".repeat(43) + "." + "b".repeat(43);
            mvc.perform(post("/api/auth/login").cookie(new Cookie("XSRF-TOKEN", forged)).header("X-XSRF-TOKEN", forged)
                    .contentType("application/json").content("{}"))
                    .andExpect(status().isForbidden());
            MvcResult login = mvc.perform(post("/api/auth/login").cookie(csrfCookie).header("X-XSRF-TOKEN", csrfValue)
                    .contentType("application/json").content(json.writeValueAsString(Map.of("email", "session@example.com", "password", "password123"))))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.expiresIn").value(900)).andReturn();
            Cookie first = login.getResponse().getCookie("refreshToken");
            assertThat(first).isNotNull();
            assertThat(login.getResponse().getHeader("Set-Cookie")).contains("HttpOnly", "SameSite=Strict");
            csrf = mvc.perform(get("/api/auth/csrf").cookie(csrfCookie, first)).andExpect(status().isOk()).andReturn();
            csrfValue = json.readTree(csrf.getResponse().getContentAsString()).get("token").asText();
            csrfCookie = csrf.getResponse().getCookie("XSRF-TOKEN");
            assertThat(csrfCookie).isNotNull();
            MvcResult refresh = mvc.perform(post("/api/auth/refresh").cookie(csrfCookie, first).header("X-XSRF-TOKEN", csrfValue))
                    .andExpect(status().isOk()).andReturn();
            String access = json.readTree(refresh.getResponse().getContentAsString()).get("accessToken").asText();
            assertThat(refresh.getResponse().getCookie("refreshToken").getValue()).isNotEqualTo(first.getValue());
            mvc.perform(post("/api/auth/refresh").cookie(csrfCookie, first).header("X-XSRF-TOKEN", csrfValue))
                    .andExpect(status().isUnauthorized());
            mvc.perform(post("/api/posts").header("Authorization", "Bearer " + access))
                    .andExpect(status().isUnauthorized());
        }

        @DisplayName("동시 회전에서 한 번만 성공하고 재사용으로 세션을 폐기한다")
        @Test void concurrentRotationRevokesSession() throws Exception {
            var token = sessions.login(910000001L);
            try {
                ExecutorService pool = Executors.newFixedThreadPool(2);
                CountDownLatch start = new CountDownLatch(1);
                try {
                    Callable<RedisSessionStore.Token> rotate = () -> { start.await(); return sessions.rotate(token.value()); };
                    var a = pool.submit(rotate); var b = pool.submit(rotate); start.countDown();
                    assertThat(java.util.stream.Stream.of(a.get(), b.get()).filter(java.util.Objects::nonNull).count()).isEqualTo(1);
                    assertThat(sessions.active(token.userId(), token.sessionId())).isFalse();
                } finally { pool.shutdownNow(); }
            } finally { redis.delete(sessions.keyForUser(910000001L)); }
        }

        @DisplayName("새 로그인은 이전 세션을 교체하며 과거 토큰 제출은 새 세션을 유지한다")
        @Test void oldSessionCannotRevokeNewLogin() {
            var old = sessions.login(910000002L);
            var current = sessions.login(910000002L);
            try {
                assertThat(sessions.active(old.userId(), old.sessionId())).isFalse();
                assertThat(sessions.rotate(old.value())).isNull();
                assertThat(sessions.active(current.userId(), current.sessionId())).isTrue();
            } finally { redis.delete(sessions.keyForUser(910000002L)); }
        }

        @DisplayName("JWT 서명 키가 교체되면 이전 Refresh 세션은 재발급할 수 없다")
        @Test void signingKeyChangeInvalidatesRefreshSessions() {
            long id = 910000006L;
            var replacement = new RedisSessionStore(redis, "other-only-v7KqX2CbM9HsR5JdW4PyT8LzF3An");
            var old = sessions.login(id);
            try {
                assertThat(replacement.rotate(old.value())).isNull();
                assertThat(replacement.active(id, old.sessionId())).isFalse();
            } finally { redis.delete(sessions.keyForUser(id)); redis.delete(replacement.keyForUser(id)); }
        }

        @DisplayName("절대 만료에 도달하면 회전과 세션 유지가 거부된다")
        @Test void absoluteExpirationStopsRotation() {
            var token = sessions.login(910000003L);
            try {
                redis.opsForHash().put(sessions.keyForUser(910000003L), "until", "1");
                assertThat(sessions.rotate(token.value())).isNull();
                assertThat(sessions.active(token.userId(), token.sessionId())).isFalse();
            } finally { redis.delete(sessions.keyForUser(910000003L)); }
        }

        @DisplayName("로그아웃은 CSRF를 검증하고 쿠키를 삭제하며 여러 번 호출할 수 있다")
        @Test void logoutRequiresCsrfAndClearsCookie() throws Exception {
            mvc.perform(post("/api/auth/logout")).andExpect(status().isForbidden());
            MvcResult csrf = mvc.perform(get("/api/auth/csrf")).andExpect(status().isOk()).andReturn();
            String token = json.readTree(csrf.getResponse().getContentAsString()).get("token").asText();
            Cookie cookie = csrf.getResponse().getCookie("XSRF-TOKEN");
            MvcResult result = mvc.perform(post("/api/auth/logout").cookie(cookie).header("X-XSRF-TOKEN", token))
                    .andExpect(status().isNoContent()).andReturn();
            assertThat(result.getResponse().getHeader("Set-Cookie")).contains("refreshToken=", "Max-Age=0", "HttpOnly");
            mvc.perform(post("/api/auth/logout").cookie(cookie).header("X-XSRF-TOKEN", token))
                    .andExpect(status().isNoContent());
        }

        @DisplayName("Access 또는 Refresh 인증으로 로그아웃하면 두 토큰을 모두 거부한다")
        @Test void authenticatedLogoutRevokesBothTokens() throws Exception {
            for (String name : java.util.List.of("logoutaccess", "logoutrefresh")) {
                mvc.perform(post("/api/auth/signup").contentType("application/json").content(json.writeValueAsString(
                        Map.of("email", name + "@example.com", "password", "password123", "nickname", name))))
                        .andExpect(status().isCreated());
                var csrf = mvc.perform(get("/api/auth/csrf")).andExpect(status().isOk()).andReturn();
                Cookie csrfCookie = csrf.getResponse().getCookie("XSRF-TOKEN");
                String csrfValue = json.readTree(csrf.getResponse().getContentAsString()).get("token").asText();
                var login = mvc.perform(post("/api/auth/login").cookie(csrfCookie).header("X-XSRF-TOKEN", csrfValue)
                        .contentType("application/json").content(json.writeValueAsString(Map.of("email", name + "@example.com", "password", "password123"))))
                        .andExpect(status().isOk()).andReturn();
                Cookie refresh = login.getResponse().getCookie("refreshToken");
                String access = json.readTree(login.getResponse().getContentAsString()).get("accessToken").asText();
                csrf = mvc.perform(get("/api/auth/csrf").cookie(csrfCookie, refresh)).andExpect(status().isOk()).andReturn();
                csrfCookie = csrf.getResponse().getCookie("XSRF-TOKEN");
                csrfValue = json.readTree(csrf.getResponse().getContentAsString()).get("token").asText();
                var logout = post("/api/auth/logout").cookie(csrfCookie, refresh).header("X-XSRF-TOKEN", csrfValue);
                if (name.equals("logoutaccess")) logout.header("Authorization", "Bearer " + access);
                mvc.perform(logout).andExpect(status().isNoContent());
                mvc.perform(post("/api/posts").header("Authorization", "Bearer " + access)).andExpect(status().isUnauthorized());
                mvc.perform(post("/api/auth/refresh").cookie(csrfCookie, refresh).header("X-XSRF-TOKEN", csrfValue))
                        .andExpect(status().isUnauthorized());
            }
        }

        @DisplayName("이전 세션의 로그아웃과 새 로그인이 경합해도 새 세션은 유지된다")
        @Test void concurrentLoginAndOldLogoutKeepNewSession() throws Exception {
            long id = 910000005L;
            ExecutorService pool = Executors.newFixedThreadPool(2);
            try {
                for (int attempt = 0; attempt < 20; attempt++) {
                    var old = sessions.login(id);
                    boolean refreshLogout = attempt % 2 == 0;
                    CountDownLatch start = new CountDownLatch(1);
                    var login = pool.submit(() -> { start.await(); return sessions.login(id); });
                    var logout = pool.submit(() -> { start.await();
                        if (refreshLogout) sessions.logoutRefresh(old.value()); else sessions.logoutAccess(id, old.sessionId());
                        return null;
                    });
                    start.countDown();
                    var current = login.get(); logout.get();
                    assertThat(sessions.active(id, current.sessionId())).isTrue();
                }
            } finally { pool.shutdownNow(); redis.delete(sessions.keyForUser(910000005L)); }
        }

        @DisplayName("회전은 7일 유휴 만료와 최초 로그인 기준 절대 만료를 넘지 않는다")
        @Test void refreshTtlHonoursBothLimits() {
            var token = sessions.login(910000004L);
            String key = sessions.keyForUser(910000004L);
            try {
                assertThat(redis.getExpire(key)).isBetween(604790L, 604800L);
                redis.opsForHash().put(key, "until", Long.toString(System.currentTimeMillis() / 1000 + 20));
                assertThat(sessions.rotate(token.value())).isNotNull();
                assertThat(redis.getExpire(key)).isBetween(1L, 20L);
            } finally { redis.delete(key); }
        }

        @DisplayName("연결할 수 없는 Redis는 세션을 인증하지 않고 오류를 반환한다")
        @Test void unavailableRedisFailsClosed() {
            var config = new org.springframework.data.redis.connection.RedisStandaloneConfiguration("127.0.0.1", 1);
            var client = org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration.builder()
                    .commandTimeout(java.time.Duration.ofSeconds(1)).build();
            var factory = new org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory(config, client);
            factory.afterPropertiesSet(); factory.start();
            try {
                var template = new org.springframework.data.redis.core.StringRedisTemplate(factory);
                template.afterPropertiesSet();
                assertThatThrownBy(() -> new RedisSessionStore(template, "test-only-q8MzV3RaL5NcH2PtJ7XyK4UwZ9Gf").active(1L, "session"))
                        .isInstanceOf(org.springframework.dao.DataAccessException.class);
            } finally { factory.destroy(); }
        }
    }
}
