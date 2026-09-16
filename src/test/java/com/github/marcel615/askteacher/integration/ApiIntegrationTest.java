package com.github.marcel615.askteacher.integration;

import com.github.marcel615.askteacher.domain.category.entity.Category;
import com.github.marcel615.askteacher.domain.post.storage.PostFileStorage;
import com.github.marcel615.askteacher.global.exception.ErrorCode;
import com.github.marcel615.askteacher.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ApiIntegrationTest extends ApiTestSupport {
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired PostFileStorage storage;

    @Test void signupLoginEncryptsPasswordAndRejectsDuplicatesAndBadCredentials() throws Exception {
        String token = signupAndLogin("alice");
        assertThat(token).isNotBlank();
        var user = users.findByEmail("alice@example.com").orElseThrow();
        assertThat(user.getPassword()).isNotEqualTo("password123");
        assertThat(passwordEncoder.matches("password123", user.getPassword())).isTrue();
        for (var entry : List.of(Map.of("email", "alice@example.com", "nickname", "other", "password", "password123"),
                Map.of("email", "other@example.com", "nickname", "alice", "password", "password123"))) {
            mvc.perform(post("/api/auth/signup").contentType("application/json").content(json.writeValueAsString(entry)))
                    .andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409));
        }
        for (var entry : List.of(Map.of("email", "missing@example.com", "password", "password123"),
                Map.of("email", "alice@example.com", "password", "wrong"))) {
            mvc.perform(post("/api/auth/login").contentType("application/json").content(json.writeValueAsString(entry)))
                    .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_LOGIN_INFO.getMessage()));
        }
        mvc.perform(post("/api/auth/signup").contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/auth/login").contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest());
        assertThat(users.count()).isEqualTo(1);
    }

    @Test void publicCategoriesMapDatabaseAndHandleEmptyList() throws Exception {
        mvc.perform(get("/api/categories")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(categoryId))
                .andExpect(jsonPath("$[0].name").value("Test Java"));
        categories.deleteAllInBatch();
        mvc.perform(get("/api/categories")).andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @Test void postLifecycleWithFilesLikesAndSoftDelete() throws Exception {
        String token = signupAndLogin("alice");
        byte[] png = {1, 2, 3};
        var created = body(mvc.perform(createRequest().file(new MockMultipartFile("files", "question.png", "image/png", png))
                        .header("Authorization", "Bearer " + token)).andExpect(status().isCreated())
                .andExpect(jsonPath("$.newPost").value(true)).andExpect(jsonPath("$.data").doesNotExist()));
        long id = created.get("postId").asLong();
        var original = posts.findById(id).orElseThrow();
        assertThat(original.isDeleted()).isFalse();
        assertThat(original.getLikeCount()).isZero();
        var stored = files.findByPostIdOrderByCreatedAtAsc(id).get(0);
        assertThat(Files.readAllBytes(Path.of(stored.getFilePath()))).containsExactly(png);
        mvc.perform(get("/api/posts/{id}", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("alice")).andExpect(jsonPath("$.categoryName").value("Test Java"))
                .andExpect(jsonPath("$.likedByMe").value(false)).andExpect(jsonPath("$.files[0].storedFileName").value(stored.getStoredFileName()))
                .andExpect(jsonPath("$.files[0].contentType").value("image/png"))
                .andExpect(jsonPath("$.files[0].fileUrl").value("/files/" + stored.getStoredFileName()));
        mvc.perform(post("/api/posts/{id}/likes", id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(content().string(""));
        mvc.perform(get("/api/posts/{id}", id).header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.likeCount").value(1)).andExpect(jsonPath("$.likedByMe").value(true));
        mvc.perform(get("/api/posts/{id}", id)).andExpect(jsonPath("$.likedByMe").value(false));
        mvc.perform(get("/api/posts")).andExpect(jsonPath("$.content[0].likeCount").value(1));
        assertThat(posts.findById(id).orElseThrow().getLikeCount()).isEqualTo(1);
        assertThat(likes.count()).isEqualTo(1);
        mvc.perform(post("/api/posts/{id}/likes", id).header("Authorization", "Bearer " + token)).andExpect(status().isBadRequest());
        assertThat(posts.findById(id).orElseThrow().getLikeCount()).isEqualTo(1);
        mvc.perform(delete("/api/posts/{id}/likes", id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
        assertThat(posts.findById(id).orElseThrow().getLikeCount()).isZero();
        assertThat(likes.count()).isZero();
        mvc.perform(get("/api/posts/{id}", id).header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.likeCount").value(0)).andExpect(jsonPath("$.likedByMe").value(false));
        mvc.perform(get("/api/posts")).andExpect(jsonPath("$.content[0].likeCount").value(0));
        mvc.perform(delete("/api/posts/{id}/likes", id).header("Authorization", "Bearer " + token)).andExpect(status().isBadRequest());
        Long nextCategory = categories.saveAndFlush(Category.createCategory("Other")).getId();
        mvc.perform(multipart(HttpMethod.PATCH, "/api/posts/{id}", id)
                        .file(new MockMultipartFile("files", "answer.pdf", "application/pdf", new byte[]{4, 5}))
                        .param("categoryId", nextCategory.toString()).param("title", "changed").param("content", "changed body")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.title").value("changed"))
                .andExpect(jsonPath("$.categoryId").value(nextCategory)).andExpect(jsonPath("$.updatedAt").isNotEmpty());
        var updated = posts.findById(id).orElseThrow();
        assertThat(updated.getCreatedAt()).isEqualTo(original.getCreatedAt());
        assertThat(updated.getUpdatedAt()).isAfter(original.getUpdatedAt());
        assertThat(updated.isNewPost()).isTrue();
        assertThat(updated.isDeleted()).isFalse();
        assertThat(updated.getUser().getId()).isEqualTo(original.getUser().getId());
        assertThat(files.findByPostIdOrderByCreatedAtAsc(id)).hasSize(2);
        mvc.perform(multipart(HttpMethod.PATCH, "/api/posts/{id}", id).param("categoryId", nextCategory.toString())
                .param("title", "without file").param("content", "body").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
        assertThat(files.findByPostIdOrderByCreatedAtAsc(id)).hasSize(2);
        mvc.perform(delete("/api/posts/{id}", id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
        var deleted = posts.findById(id).orElseThrow();
        assertThat(deleted.isDeleted()).isTrue();
        assertThat(deleted.getUpdatedAt()).isAfter(updated.getUpdatedAt());
        mvc.perform(get("/api/posts/{id}", id)).andExpect(status().isNotFound());
        mvc.perform(get("/api/posts")).andExpect(jsonPath("$.content").isEmpty());
        mvc.perform(delete("/api/posts/{id}", id).header("Authorization", "Bearer " + token)).andExpect(status().isNotFound());
        mvc.perform(post("/api/posts/{id}/likes", id).header("Authorization", "Bearer " + token)).andExpect(status().isNotFound());
        mvc.perform(delete("/api/posts/{id}/likes", id).header("Authorization", "Bearer " + token)).andExpect(status().isNotFound());
    }

    @Test void searchPagingCategoryCombinationsAndBlankKeyword() throws Exception {
        String token = signupAndLogin("alice");
        Long oldId = createPost(token);
        var old = posts.findById(oldId).orElseThrow();
        ReflectionTestUtils.setField(old, "createdAt", LocalDateTime.now().minusDays(1));
        posts.saveAndFlush(old);
        Long recentId = createPost(token);
        Long otherCategory = categories.saveAndFlush(Category.createCategory("Other")).getId();
        Long filteredCategory = categoryId;
        categoryId = otherCategory;
        Long otherId = createPost(token);
        categoryId = filteredCategory;
        mvc.perform(get("/api/posts").param("keyword", " Spring ").param("categoryId", categoryId.toString()).param("size", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].postId").value(recentId))
                .andExpect(jsonPath("$.totalElements").value(2)).andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.hasNext").value(true)).andExpect(jsonPath("$.isFirst").value(true));
        mvc.perform(get("/api/posts").param("categoryId", categoryId.toString()).param("page", "1").param("size", "1"))
                .andExpect(jsonPath("$.content[0].postId").value(oldId)).andExpect(jsonPath("$.hasPrevious").value(true))
                .andExpect(jsonPath("$.isLast").value(true));
        mvc.perform(get("/api/posts").param("keyword", "  ")).andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content[0].postId").value(otherId));
        mvc.perform(get("/api/posts").param("keyword", "body")).andExpect(jsonPath("$.totalElements").value(3));
        mvc.perform(get("/api/posts").param("keyword", "absent")).andExpect(jsonPath("$.content").isEmpty());
        mvc.perform(get("/api/posts").param("page", "10")).andExpect(jsonPath("$.content").isEmpty());
        mvc.perform(get("/api/posts").param("page", "-1")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/posts").param("size", "0")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/posts").param("categoryId", "-1")).andExpect(status().isNotFound());
        mvc.perform(get("/api/posts").param("page", "bad")).andExpect(status().isBadRequest());
    }

    @Test void missingResourcesWrongAuthorAndInvalidInputDoNotModifyPost() throws Exception {
        String token = signupAndLogin("alice");
        String other = signupAndLogin("bob");
        Long id = createPost(token);
        mvc.perform(multipart(HttpMethod.PATCH, "/api/posts/{id}", id).param("categoryId", categoryId.toString())
                .param("title", "changed").param("content", "body").header("Authorization", "Bearer " + other))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value(ErrorCode.POST_AUTHOR_MISMATCH.getMessage()));
        mvc.perform(delete("/api/posts/{id}", id).header("Authorization", "Bearer " + other))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value(ErrorCode.POST_AUTHOR_MISMATCH.getMessage()));
        mvc.perform(get("/api/posts/-1")).andExpect(status().isNotFound());
        mvc.perform(delete("/api/posts/-1").header("Authorization", "Bearer " + token)).andExpect(status().isNotFound());
        mvc.perform(post("/api/posts/-1/likes").header("Authorization", "Bearer " + token)).andExpect(status().isNotFound());
        mvc.perform(delete("/api/posts/-1/likes").header("Authorization", "Bearer " + token)).andExpect(status().isNotFound());
        mvc.perform(multipart(HttpMethod.PATCH, "/api/posts/-1").param("categoryId", categoryId.toString())
                .param("title", "changed").param("content", "body").header("Authorization", "Bearer " + token)).andExpect(status().isNotFound());
        for (var method : List.of(HttpMethod.POST, HttpMethod.PATCH)) {
            String path = method == HttpMethod.POST ? "/api/posts" : "/api/posts/" + id;
            mvc.perform(multipart(method, path).param("categoryId", "-1").param("title", "title").param("content", "body")
                    .header("Authorization", "Bearer " + token)).andExpect(status().isNotFound());
            mvc.perform(multipart(method, path).param("categoryId", categoryId.toString()).param("title", " ").param("content", "body")
                    .header("Authorization", "Bearer " + token)).andExpect(status().isBadRequest());
        }
        assertThat(posts.findById(id).orElseThrow().getTitle()).isEqualTo("Spring question");
        assertThat(posts.count()).isEqualTo(1);
    }

    @Test void invalidFilesRollbackCreateAndUpdate() throws Exception {
        String token = signupAndLogin("alice");
        Long id = createPost(token);
        for (var file : List.of(new MockMultipartFile("files", "a.txt", "text/plain", new byte[]{1}),
                new MockMultipartFile("files", "empty.png", "image/png", new byte[0]),
                new MockMultipartFile("files", "large.pdf", "application/pdf", new byte[10485761]))) {
            for (var method : List.of(HttpMethod.POST, HttpMethod.PATCH)) {
                mvc.perform(multipart(method, method == HttpMethod.POST ? "/api/posts" : "/api/posts/" + id).file(file)
                        .param("categoryId", categoryId.toString()).param("title", "changed").param("content", "body")
                        .header("Authorization", "Bearer " + token)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
            }
        }
        assertThat(posts.count()).isEqualTo(1);
        assertThat(posts.findById(id).orElseThrow().getTitle()).isEqualTo("Spring question");
        assertThat(files.count()).isZero();
        try (var paths = Files.list(uploadDirectory)) { assertThat(paths).isEmpty(); }
    }

    @Test void realStorageFailureReturns500AndRollsBackDatabase() throws Exception {
        String token = signupAndLogin("alice");
        Long id = createPost(token);
        Path blockedPath = uploadDirectory.resolve("not-a-directory");
        Files.writeString(blockedPath, "block directory creation");
        // Deterministic I/O failure without OS-specific permission changes or a mocked storage service.
        Object originalPath = ReflectionTestUtils.getField(storage, "storagePath");
        ReflectionTestUtils.setField(storage, "storagePath", blockedPath);
        try {
            for (var method : List.of(HttpMethod.POST, HttpMethod.PATCH)) {
                mvc.perform(multipart(method, method == HttpMethod.POST ? "/api/posts" : "/api/posts/" + id)
                        .file(new MockMultipartFile("files", "a.png", "image/png", new byte[]{1}))
                        .param("categoryId", categoryId.toString()).param("title", "changed").param("content", "body")
                        .header("Authorization", "Bearer " + token)).andExpect(status().isInternalServerError())
                        .andExpect(jsonPath("$.status").value(500)).andExpect(jsonPath("$.message").value(ErrorCode.FILE_STORAGE_FAILED.getMessage()));
            }
            assertThat(posts.count()).isEqualTo(1);
            assertThat(posts.findById(id).orElseThrow().getTitle()).isEqualTo("Spring question");
            assertThat(files.count()).isZero();
        } finally {
            ReflectionTestUtils.setField(storage, "storagePath", originalPath);
        }
    }

    @ParameterizedTest
    @CsvSource({
            "POST, /api/posts, missing", "POST, /api/posts, invalid",
            "PATCH, /api/posts/1, missing", "PATCH, /api/posts/1, invalid",
            "DELETE, /api/posts/1, missing", "DELETE, /api/posts/1, invalid",
            "POST, /api/posts/1/likes, missing", "POST, /api/posts/1/likes, invalid",
            "DELETE, /api/posts/1/likes, missing", "DELETE, /api/posts/1/likes, invalid"
    })
    void protectedApisRejectMissingOrInvalidTokenAccordingToSpecification(String method, String path, String token) throws Exception {
        // api-spec.md specifies 401; do not loosen this assertion to hide a mismatch.
        MockHttpServletRequestBuilder request = request(HttpMethod.valueOf(method), path);
        if (token.equals("invalid")) { request.header("Authorization", "Bearer invalid-token"); }
        mvc.perform(request).andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(content().encoding("UTF-8"))
                .andExpect(content().json("{\"status\":401,\"message\":\"인증이 필요합니다.\"}"));
    }
}
