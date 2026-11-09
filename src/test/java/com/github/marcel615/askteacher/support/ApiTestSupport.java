package com.github.marcel615.askteacher.support;

import com.github.marcel615.askteacher.domain.auth.session.RedisSessionStore;
import com.github.marcel615.askteacher.domain.category.entity.Category;
import com.github.marcel615.askteacher.domain.category.repository.CategoryRepository;
import com.github.marcel615.askteacher.domain.post.repository.PostFileRepository;
import com.github.marcel615.askteacher.domain.post.repository.PostRepository;
import com.github.marcel615.askteacher.domain.postlike.repository.PostLikeRepository;
import com.github.marcel615.askteacher.domain.user.entity.User;
import com.github.marcel615.askteacher.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class ApiTestSupport {
    @TempDir
    protected static Path uploadDirectory;
    @Autowired
    protected MockMvc mvc;
    @Autowired
    protected ObjectMapper json;
    @Autowired
    protected UserRepository users;
    @Autowired
    protected CategoryRepository categories;
    @Autowired
    protected PostRepository posts;
    @Autowired
    protected PostFileRepository files;
    @Autowired
    protected PostLikeRepository likes;
    @Autowired
    protected StringRedisTemplate redis;
    @Autowired
    protected RedisSessionStore sessionStore;
    protected Long categoryId;

    @DynamicPropertySource
    static void isolatedStorage(DynamicPropertyRegistry registry) {
        registry.add("app.file-storage.post-upload-dir", () -> uploadDirectory.toString());
    }

    @BeforeEach
    void prepareDatabase() throws Exception {
        clean();
        categoryId = categories.saveAndFlush(Category.createCategory("Test Java")).getId();
    }

    @AfterEach
    void clean() throws Exception {
        // Real HTTP requests commit outside the test thread; rollback cannot clean them.
        for (User user : users.findAll()) {
            redis.delete(sessionStore.keyForUser(user.getId()));
        }
        likes.deleteAllInBatch();
        files.deleteAllInBatch();
        posts.deleteAllInBatch();
        users.deleteAllInBatch();
        categories.deleteAllInBatch();
        try (Stream<Path> paths = Files.list(uploadDirectory)) {
            for (Path path : paths.toList()) {
                Files.delete(path);
            }
        }
    }

    protected String signupAndLogin(String name) throws Exception {
        Map<String, String> signupBody = Map.of(
                "email", name + "@example.com",
                "password", "password123",
                "nickname", name
        );
        String signupJson = json.writeValueAsString(signupBody);
        mvc.perform(post("/api/auth/signup")
                        .contentType("application/json")
                        .content(signupJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").isNumber());

        Map<String, String> loginBody = Map.of("email", name + "@example.com", "password", "password123");
        String loginJson = json.writeValueAsString(loginBody);
        ResultActions loginResult = mvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType("application/json")
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));

        JsonNode loginResponse = body(loginResult);
        return loginResponse.get("accessToken").asText();
    }

    protected MockMultipartHttpServletRequestBuilder createRequest() {
        return multipart("/api/posts")
                .param("categoryId", categoryId.toString())
                .param("title", "Spring question")
                .param("content", "question body");
    }

    protected Long createPost(String token) throws Exception {
        ResultActions result = mvc.perform(createRequest().header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());
        JsonNode response = body(result);
        return response.get("postId").asLong();
    }

    protected JsonNode body(ResultActions result) throws Exception {
        MvcResult mvcResult = result.andReturn();
        String responseBody = mvcResult.getResponse().getContentAsString();
        return json.readTree(responseBody);
    }
}
