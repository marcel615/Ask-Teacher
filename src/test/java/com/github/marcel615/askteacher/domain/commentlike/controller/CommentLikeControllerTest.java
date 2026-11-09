package com.github.marcel615.askteacher.domain.commentlike.controller;

import com.github.marcel615.askteacher.domain.commentlike.dto.CommentLikeResponse;
import com.github.marcel615.askteacher.domain.commentlike.service.CommentLikeService;
import com.github.marcel615.askteacher.global.exception.CustomException;
import com.github.marcel615.askteacher.global.exception.ErrorCode;
import com.github.marcel615.askteacher.support.WebTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentLikeController.class)
class CommentLikeControllerTest extends WebTestSupport {
    @Nested
    @DisplayName("댓글 좋아요 API")
    class Scenarios {

        @MockitoBean CommentLikeService service;

        @DisplayName("댓글 좋아요와 취소는 변경된 개수를 반환한다")
        @Test void likeAndUnlikeReturnUpdatedCounts() throws Exception {
            when(service.likeComment(5L, 7L)).thenReturn(new CommentLikeResponse(5L, 2, true));
            when(service.unlikeComment(5L, 7L)).thenReturn(new CommentLikeResponse(5L, 1, false));
            mvc.perform(post("/api/comments/5/likes").with(authenticated()))
                    .andExpect(status().isCreated()).andExpect(jsonPath("$.likeCount").value(2))
                    .andExpect(jsonPath("$.likedByMe").value(true));
            mvc.perform(delete("/api/comments/5/likes").with(authenticated()))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.likeCount").value(1))
                    .andExpect(jsonPath("$.likedByMe").value(false));
            verify(service).likeComment(5L, 7L);
            verify(service).unlikeComment(5L, 7L);
        }

        @DisplayName("정의된 비즈니스 오류를 응답으로 변환한다")
        @Test void mapsDefinedBusinessErrors() throws Exception {
            when(service.likeComment(5L, 7L)).thenThrow(new CustomException(ErrorCode.DUPLICATE_COMMENT_LIKE));
            when(service.unlikeComment(5L, 7L)).thenThrow(new CustomException(ErrorCode.COMMENT_LIKE_NOT_FOUND));
            mvc.perform(post("/api/comments/5/likes").with(authenticated()))
                    .andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409));
            mvc.perform(delete("/api/comments/5/likes").with(authenticated()))
                    .andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404));
        }
    }
}
