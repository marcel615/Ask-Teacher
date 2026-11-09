package com.github.marcel615.askteacher.global.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityConfigTest {
    @Nested
    @DisplayName("API 접근 인증")
    class Scenarios {

        private final HttpClient httpClient = HttpClient.newHttpClient();

        @LocalServerPort
        private int port;

        @Autowired
        private ObjectMapper objectMapper;

        @DisplayName("공개 조회 API는 인증을 요구하지 않는다")
        @Test
        void publicReadEndpointsDoNotRequireAuthentication() throws Exception {
            assertThat(statusOfGet("/api/categories")).isEqualTo(200);
            assertThat(statusOfGet("/api/posts")).isEqualTo(200);
            assertThat(statusOfGet("/api/posts/999999")).isEqualTo(404);
            assertThat(statusOfGet("/api/posts/999999/comments")).isEqualTo(404);
        }

        @DisplayName("게시글 작성 API는 인증을 요구한다")
        @ParameterizedTest
        @ValueSource(strings = {"", "Bearer invalid-token"})
        void postWriteEndpointRequiresAuthentication(String authorization) throws Exception {
            HttpRequest.Builder builder = HttpRequest.newBuilder(uri("/api/posts"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{}"));
            if (!authorization.isEmpty()) {
                builder.header("Authorization", authorization);
            }
            HttpRequest request = builder.build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            assertThat(response.statusCode()).isEqualTo(401);
            MediaType contentType = MediaType.parseMediaType(response.headers().firstValue("Content-Type").orElseThrow());
            assertThat(contentType.isCompatibleWith(MediaType.APPLICATION_JSON)).isTrue();
            assertThat(contentType.getCharset()).isEqualTo(java.nio.charset.StandardCharsets.UTF_8);
            assertThat(objectMapper.readTree(response.body()))
                    .isEqualTo(objectMapper.readTree("{\"status\":401,\"message\":\"인증이 필요합니다.\"}"));
        }

        @DisplayName("허용된 Origin의 자격 증명 요청만 CORS 사전 요청을 통과한다")
        @Test void corsAllowsConfiguredOriginOnly() throws Exception {
            HttpRequest allowed = HttpRequest.newBuilder(uri("/api/auth/refresh"))
                    .header("Origin", "http://localhost:5173")
                    .header("Access-Control-Request-Method", "POST")
                    .method("OPTIONS", HttpRequest.BodyPublishers.noBody()).build();
            var allowedResponse = httpClient.send(allowed, HttpResponse.BodyHandlers.ofString());
            assertThat(allowedResponse.statusCode()).isEqualTo(200);
            assertThat(allowedResponse.headers().firstValue("Access-Control-Allow-Credentials")).hasValue("true");
            HttpRequest denied = HttpRequest.newBuilder(uri("/api/auth/refresh"))
                    .header("Origin", "https://untrusted.example")
                    .header("Access-Control-Request-Method", "POST")
                    .method("OPTIONS", HttpRequest.BodyPublishers.noBody()).build();
            assertThat(httpClient.send(denied, HttpResponse.BodyHandlers.ofString()).statusCode()).isEqualTo(403);
        }

        @DisplayName("실제 HTTP 응답은 CSRF 쿠키의 SameSite와 HttpOnly 속성을 포함한다")
        @Test void csrfCookieAttributesAreSentOverHttp() throws Exception {
            var response = httpClient.send(HttpRequest.newBuilder(uri("/api/auth/csrf")).GET().build(), HttpResponse.BodyHandlers.ofString());
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.headers().firstValue("Set-Cookie")).hasValueSatisfying(cookie ->
                    assertThat(cookie).contains("XSRF-TOKEN=", "HttpOnly", "SameSite=Strict"));
        }

        private int statusOfGet(String path) throws IOException, InterruptedException {
            HttpRequest request = HttpRequest.newBuilder(uri(path)).GET().build();

            return httpClient.send(request, HttpResponse.BodyHandlers.ofString()).statusCode();
        }

        private URI uri(String path) {
            return URI.create("http://localhost:" + port + path);
        }
    }
}
