package com.github.marcel615.askteacher.integration;

import com.github.marcel615.askteacher.global.exception.ErrorCode;
import com.github.marcel615.askteacher.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UploadLimitIntegrationTest extends ApiTestSupport {
    @LocalServerPort int port;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    @Test void realServletAcceptsExactLimitAndRejectsOversizedCreateAndPatch() throws Exception {
        String token = signupAndLogin("uploader");
        var accepted = send("POST", "/api/posts", token, 10485760);
        assertThat(accepted.statusCode()).isEqualTo(201);
        long id = json.readTree(accepted.body()).get("postId").asLong();
        assertThat(files.findByPostIdOrderByCreatedAtAsc(id)).hasSize(1);
        assertThat(files.findByPostIdOrderByCreatedAtAsc(id).get(0).getFileSize()).isEqualTo(10485760);
        for (String method : new String[]{"POST", "PATCH"}) {
            var rejected = send(method, method.equals("POST") ? "/api/posts" : "/api/posts/" + id, token, 10485761);
            assertThat(rejected.statusCode()).isEqualTo(400);
            assertThat(json.readTree(rejected.body()).get("message").asText()).isEqualTo(ErrorCode.FILE_SIZE_EXCEEDED.getMessage());
        }
        assertThat(posts.count()).isEqualTo(1);
        assertThat(files.count()).isEqualTo(1);
    }

    private HttpResponse<String> send(String method, String path, String token, int size) throws Exception {
        String boundary = "test-upload-boundary";
        String prefix = "--" + boundary + "\r\nContent-Disposition: form-data; name=\"categoryId\"\r\n\r\n" + categoryId
                + "\r\n--" + boundary + "\r\nContent-Disposition: form-data; name=\"title\"\r\n\r\nUpload test"
                + "\r\n--" + boundary + "\r\nContent-Disposition: form-data; name=\"content\"\r\n\r\nBody"
                + "\r\n--" + boundary + "\r\nContent-Disposition: form-data; name=\"files\"; filename=\"test.pdf\"\r\nContent-Type: application/pdf\r\n\r\n";
        byte[] data = new byte[size];
        Arrays.fill(data, (byte) 'x');
        var body = HttpRequest.BodyPublishers.concat(HttpRequest.BodyPublishers.ofString(prefix, StandardCharsets.UTF_8),
                HttpRequest.BodyPublishers.ofByteArray(data), HttpRequest.BodyPublishers.ofString("\r\n--" + boundary + "--\r\n"));
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).timeout(Duration.ofSeconds(30))
                .header("Authorization", "Bearer " + token).header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .method(method, body).build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
