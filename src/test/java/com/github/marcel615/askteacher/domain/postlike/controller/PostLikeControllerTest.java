package com.github.marcel615.askteacher.domain.postlike.controller;

import com.github.marcel615.askteacher.domain.postlike.service.PostLikeService;
import com.github.marcel615.askteacher.global.exception.*;
import com.github.marcel615.askteacher.support.WebTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostLikeController.class)
class PostLikeControllerTest extends WebTestSupport {
    @Nested
    @DisplayName("게시글 좋아요 API")
    class Scenarios {
        @MockitoBean PostLikeService service;

        @DisplayName("인증 사용자를 전달하고 빈 응답 본문을 반환한다")
        @Test void passesAuthenticatedUserAndReturnsEmptyBodies() throws Exception {
            mvc.perform(post("/api/posts/3/likes").with(authenticated())).andExpect(status().isOk()).andExpect(content().string(""));
            mvc.perform(delete("/api/posts/3/likes").with(authenticated())).andExpect(status().isNoContent()).andExpect(content().string(""));
            verify(service).likePost(3L, 7L);
            verify(service).unlikePost(3L, 7L);
        }

        @DisplayName("비즈니스 오류와 잘못된 경로를 응답으로 변환한다")
        @Test void mapsBusinessErrorsAndInvalidPath() throws Exception {
            doThrow(new CustomException(ErrorCode.INVALID_INPUT_VALUE)).when(service).likePost(3L, 7L);
            doThrow(new CustomException(ErrorCode.POST_NOT_FOUND)).when(service).unlikePost(3L, 7L);
            mvc.perform(post("/api/posts/3/likes").with(authenticated())).andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
            mvc.perform(delete("/api/posts/3/likes").with(authenticated())).andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404));
            mvc.perform(post("/api/posts/not-a-number/likes").with(authenticated())).andExpect(status().isBadRequest());
        }
    }
}
