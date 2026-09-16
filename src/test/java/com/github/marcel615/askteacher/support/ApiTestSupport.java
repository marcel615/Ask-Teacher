package com.github.marcel615.askteacher.support;

import com.github.marcel615.askteacher.domain.category.entity.Category;
import com.github.marcel615.askteacher.domain.category.repository.CategoryRepository;
import com.github.marcel615.askteacher.domain.post.repository.*;
import com.github.marcel615.askteacher.domain.postlike.repository.PostLikeRepository;
import com.github.marcel615.askteacher.domain.user.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.*;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import tools.jackson.databind.*;
import java.nio.file.*;
import java.util.Map;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class ApiTestSupport {
    @TempDir protected static Path uploadDirectory;
    @Autowired protected MockMvc mvc;
    @Autowired protected ObjectMapper json;
    @Autowired protected UserRepository users;
    @Autowired protected CategoryRepository categories;
    @Autowired protected PostRepository posts;
    @Autowired protected PostFileRepository files;
    @Autowired protected PostLikeRepository likes;
    protected Long categoryId;

    @DynamicPropertySource
    static void isolatedStorage(DynamicPropertyRegistry registry) {
        registry.add("app.file-storage.post-upload-dir", () -> uploadDirectory.toString());
    }

    @BeforeEach void prepareDatabase() throws Exception {
        clean();
        categoryId = categories.saveAndFlush(Category.createCategory("Test Java")).getId();
    }

    @AfterEach void clean() throws Exception {
        // Real HTTP requests commit outside the test thread; rollback cannot clean them.
        likes.deleteAllInBatch();
        files.deleteAllInBatch();
        posts.deleteAllInBatch();
        users.deleteAllInBatch();
        categories.deleteAllInBatch();
        try (var paths = Files.list(uploadDirectory)) {
            for (Path path : paths.toList()) { Files.delete(path); }
        }
    }

    protected String signupAndLogin(String name) throws Exception {
        mvc.perform(post("/api/auth/signup").contentType("application/json").content(json.writeValueAsString(
                Map.of("email", name + "@example.com", "password", "password123", "nickname", name))))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.userId").isNumber());
        return body(mvc.perform(post("/api/auth/login").contentType("application/json").content(json.writeValueAsString(
                Map.of("email", name + "@example.com", "password", "password123"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.tokenType").value("Bearer")))
                .get("accessToken").asText();
    }

    protected MockMultipartHttpServletRequestBuilder createRequest() {
        return multipart("/api/posts").param("categoryId", categoryId.toString()).param("title", "Spring question").param("content", "question body");
    }

    protected Long createPost(String token) throws Exception {
        return body(mvc.perform(createRequest().header("Authorization", "Bearer " + token)).andExpect(status().isCreated()))
                .get("postId").asLong();
    }

    protected JsonNode body(ResultActions result) throws Exception {
        return json.readTree(result.andReturn().getResponse().getContentAsString());
    }
}
