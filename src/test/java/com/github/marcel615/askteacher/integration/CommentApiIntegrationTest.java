package com.github.marcel615.askteacher.integration;

import com.github.marcel615.askteacher.domain.comment.repository.CommentRepository;
import com.github.marcel615.askteacher.domain.commentlike.repository.CommentLikeRepository;
import com.github.marcel615.askteacher.global.exception.ErrorCode;
import com.github.marcel615.askteacher.support.ApiTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CommentApiIntegrationTest extends ApiTestSupport {

    @Autowired CommentRepository comments;
    @Autowired CommentLikeRepository commentLikes;

    @BeforeEach
    @AfterEach
    void cleanComments() {
        commentLikes.deleteAllInBatch();
        comments.deleteAllInBatch();
    }

    @Test void authenticatedCommentLifecycleEnforcesValidationAuthorAndSoftDelete() throws Exception {
        String alice = signupAndLogin("alice");
        String bob = signupAndLogin("bob");
        Long postId = createPost(alice);

        var created = body(mvc.perform(post("/api/posts/{postId}/comments", postId)
                        .header("Authorization", "Bearer " + alice).contentType("application/json")
                        .content(json.writeValueAsString(Map.of("content", "  first comment  "))))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.content").value("first comment"))
                .andExpect(jsonPath("$.likeCount").value(0)).andExpect(jsonPath("$.likedByMe").value(false)));
        long commentId = created.get("commentId").asLong();
        assertThat(comments.findById(commentId).orElseThrow().isDeleted()).isFalse();

        mvc.perform(patch("/api/comments/{commentId}", commentId).header("Authorization", "Bearer " + bob)
                        .contentType("application/json").content(json.writeValueAsString(Map.of("content", "blocked"))))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.message").value(ErrorCode.COMMENT_AUTHOR_MISMATCH.getMessage()));
        mvc.perform(patch("/api/comments/{commentId}", commentId).header("Authorization", "Bearer " + alice)
                        .contentType("application/json").content(json.writeValueAsString(Map.of("content", " changed "))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content").value("changed"));
        mvc.perform(patch("/api/comments/{commentId}", commentId).header("Authorization", "Bearer " + alice)
                        .contentType("application/json").content(json.writeValueAsString(Map.of("content", " "))))
                .andExpect(status().isBadRequest());

        mvc.perform(post("/api/comments/{commentId}/likes", commentId).header("Authorization", "Bearer " + bob))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.likeCount").value(1))
                .andExpect(jsonPath("$.likedByMe").value(true));
        mvc.perform(post("/api/comments/{commentId}/likes", commentId).header("Authorization", "Bearer " + bob))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409));
        mvc.perform(delete("/api/comments/{commentId}", commentId).header("Authorization", "Bearer " + alice))
                .andExpect(status().isNoContent());

        assertThat(comments.findById(commentId).orElseThrow().isDeleted()).isTrue();
        assertThat(commentLikes.count()).isZero();
        mvc.perform(get("/api/posts/{postId}/comments", postId)).andExpect(status().isOk())
                .andExpect(jsonPath("$.comments").isEmpty());
        mvc.perform(patch("/api/comments/{commentId}", commentId).header("Authorization", "Bearer " + alice)
                        .contentType("application/json").content(json.writeValueAsString(Map.of("content", "again"))))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/comments/{commentId}/likes", commentId).header("Authorization", "Bearer " + bob))
                .andExpect(status().isNotFound());
    }

    @Test void publicAndAuthenticatedListsApplyPagingSortingAndLikedByMe() throws Exception {
        String alice = signupAndLogin("alice");
        String bob = signupAndLogin("bob");
        Long postId = createPost(alice);
        long first = createComment(postId, alice, "first");
        Thread.sleep(5);
        long second = createComment(postId, alice, "second");

        mvc.perform(post("/api/comments/{id}/likes", first).header("Authorization", "Bearer " + alice))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/comments/{id}/likes", first).header("Authorization", "Bearer " + bob))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/comments/{id}/likes", second).header("Authorization", "Bearer " + bob))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/posts/{postId}/comments", postId).param("size", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.comments[0].commentId").value(second))
                .andExpect(jsonPath("$.comments[0].likedByMe").value(false))
                .andExpect(jsonPath("$.totalElements").value(2)).andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.hasNext").value(true)).andExpect(jsonPath("$.sort").value("latest"));
        mvc.perform(get("/api/posts/{postId}/comments", postId).param("sort", "likeCount")
                        .header("Authorization", "Bearer " + alice))
                .andExpect(status().isOk()).andExpect(jsonPath("$.comments[0].commentId").value(first))
                .andExpect(jsonPath("$.comments[0].likeCount").value(2))
                .andExpect(jsonPath("$.comments[0].likedByMe").value(true));

        for (var params : new String[][]{{"page", "-1"}, {"size", "0"}, {"size", "101"}, {"sort", "bad"}}) {
            mvc.perform(get("/api/posts/{postId}/comments", postId).param(params[0], params[1]))
                    .andExpect(status().isBadRequest());
        }
        mvc.perform(get("/api/posts/999999/comments")).andExpect(status().isNotFound());

        mvc.perform(delete("/api/comments/{id}/likes", first).header("Authorization", "Bearer " + alice))
                .andExpect(status().isOk()).andExpect(jsonPath("$.likeCount").value(1))
                .andExpect(jsonPath("$.likedByMe").value(false));
        mvc.perform(delete("/api/comments/{id}/likes", first).header("Authorization", "Bearer " + alice))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404));
    }

    @Test void protectedCommentApisRejectMissingAndInvalidTokens() throws Exception {
        for (String[] endpoint : new String[][]{
                {"POST", "/api/posts/1/comments"}, {"PATCH", "/api/comments/1"},
                {"DELETE", "/api/comments/1"}, {"POST", "/api/comments/1/likes"},
                {"DELETE", "/api/comments/1/likes"}}) {
            for (String token : new String[]{"", "invalid-token"}) {
                MockHttpServletRequestBuilder request = request(HttpMethod.valueOf(endpoint[0]), endpoint[1])
                        .contentType("application/json").content("{\"content\":\"body\"}");
                if (!token.isEmpty()) {
                    request.header("Authorization", "Bearer " + token);
                }
                mvc.perform(request).andExpect(status().isUnauthorized())
                        .andExpect(jsonPath("$.status").value(401));
            }
        }
    }

    private long createComment(Long postId, String token, String content) throws Exception {
        return body(mvc.perform(post("/api/posts/{postId}/comments", postId)
                        .header("Authorization", "Bearer " + token).contentType("application/json")
                        .content(json.writeValueAsString(Map.of("content", content))))
                .andExpect(status().isCreated())).get("commentId").asLong();
    }
}
