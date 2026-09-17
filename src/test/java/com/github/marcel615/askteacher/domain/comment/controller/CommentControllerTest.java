package com.github.marcel615.askteacher.domain.comment.controller;

import com.github.marcel615.askteacher.domain.comment.dto.CommentPageResponse;
import com.github.marcel615.askteacher.domain.comment.dto.CommentResponse;
import com.github.marcel615.askteacher.domain.comment.service.CommentService;
import com.github.marcel615.askteacher.global.exception.CustomException;
import com.github.marcel615.askteacher.global.exception.ErrorCode;
import com.github.marcel615.askteacher.support.WebTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
class CommentControllerTest extends WebTestSupport {

    @MockitoBean CommentService service;

    @Test void createUpdateAndDeleteBindPrincipalAndBody() throws Exception {
        var response = response("content", false);
        when(service.createComment(eq(3L), eq(7L), any())).thenReturn(response);
        when(service.updateComment(eq(5L), eq(7L), any())).thenReturn(response("changed", true));

        mvc.perform(post("/api/posts/3/comments").with(authenticated()).contentType("application/json")
                        .content("{\"content\":\" content \"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.commentId").value(5))
                .andExpect(jsonPath("$.likedByMe").value(false));
        mvc.perform(patch("/api/comments/5").with(authenticated()).contentType("application/json")
                        .content("{\"content\":\"changed\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content").value("changed"));
        mvc.perform(delete("/api/comments/5").with(authenticated()))
                .andExpect(status().isNoContent()).andExpect(content().string(""));

        verify(service).createComment(eq(3L), eq(7L), argThat(request -> request.content().equals(" content ")));
        verify(service).updateComment(eq(5L), eq(7L), argThat(request -> request.content().equals("changed")));
        verify(service).deleteComment(5L, 7L);
    }

    @Test void publicListUsesDefaultsAndOptionalPrincipal() throws Exception {
        when(service.getComments(3L, null, 0, 20, "latest"))
                .thenReturn(new CommentPageResponse(List.of(), 0, 20, 0, 0, false, "latest"));
        mvc.perform(get("/api/posts/3/comments"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.comments").isEmpty())
                .andExpect(jsonPath("$.page").value(0)).andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.sort").value("latest"));
        mvc.perform(get("/api/posts/3/comments").with(authenticated())
                        .param("page", "1").param("size", "5").param("sort", "likeCount"))
                .andExpect(status().isOk());
        verify(service).getComments(3L, 7L, 1, 5, "likeCount");
    }

    @Test void validationAndBusinessErrorsUseErrorContract() throws Exception {
        for (String content : List.of("", " ", "x".repeat(1001))) {
            mvc.perform(post("/api/posts/3/comments").with(authenticated()).contentType("application/json")
                            .content("{\"content\":\"" + content + "\"}"))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
        }
        when(service.getComments(3L, null, 0, 20, "bad"))
                .thenThrow(new CustomException(ErrorCode.INVALID_INPUT_VALUE));
        mvc.perform(get("/api/posts/3/comments").param("sort", "bad"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
        verify(service, never()).createComment(any(), any(), any());
    }

    private CommentResponse response(String content, boolean likedByMe) {
        LocalDateTime now = LocalDateTime.of(2026, 9, 16, 12, 0);
        return new CommentResponse(5L, 3L, 7L, "writer", content, likedByMe ? 1 : 0, likedByMe, now, now);
    }
}
