package com.github.marcel615.askteacher.global.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityConfigTest {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @LocalServerPort
    private int port;

    @Test
    void publicReadEndpointsDoNotRequireAuthentication() throws Exception {
        assertThat(statusOfGet("/api/categories")).isEqualTo(200);
        assertThat(statusOfGet("/api/posts")).isEqualTo(200);
        assertThat(statusOfGet("/api/posts/999999")).isEqualTo(404);
    }

    @Test
    void postWriteEndpointRequiresAuthentication() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(uri("/api/posts"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{}"))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isIn(401, 403);
    }

    private int statusOfGet(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(uri(path)).GET().build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString()).statusCode();
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }
}
