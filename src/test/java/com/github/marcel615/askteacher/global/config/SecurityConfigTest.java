package com.github.marcel615.askteacher.global.config;

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

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void publicReadEndpointsDoNotRequireAuthentication() throws Exception {
        assertThat(statusOfGet("/api/categories")).isEqualTo(200);
        assertThat(statusOfGet("/api/posts")).isEqualTo(200);
        assertThat(statusOfGet("/api/posts/999999")).isEqualTo(404);
        assertThat(statusOfGet("/api/posts/999999/comments")).isEqualTo(404);
    }

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

    private int statusOfGet(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(uri(path)).GET().build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString()).statusCode();
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }
}
