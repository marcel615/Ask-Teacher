package com.github.marcel615.askteacher.domain.post.controller;

import com.github.marcel615.askteacher.domain.post.dto.*;
import com.github.marcel615.askteacher.domain.post.service.PostService;
import com.github.marcel615.askteacher.global.exception.*;
import com.github.marcel615.askteacher.support.WebTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.http.HttpMethod;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostController.class)
class PostControllerTest extends WebTestSupport {
    @MockitoBean PostService service;

    @Test void multipartCreateAndPatchBindFieldsFilesAndPrincipal() throws Exception {
        var now = LocalDateTime.of(2026, 1, 1, 0, 0);
        var file = new MockMultipartFile("files", "a.png", "image/png", new byte[]{1});
        when(service.createPost(eq(7L), eq(new PostCreateRequest(2L, "title", "body")), any()))
                .thenReturn(new PostCreateResponse(3L, "title", true, now));
        mvc.perform(multipart("/api/posts").file(file).param("categoryId", "2").param("title", "title").param("content", "body").with(authenticated()))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.postId").value(3)).andExpect(jsonPath("$.title").value("title"))
                .andExpect(jsonPath("$.newPost").value(true)).andExpect(jsonPath("$.createdAt").exists()).andExpect(jsonPath("$.data").doesNotExist());
        verify(service).createPost(7L, new PostCreateRequest(2L, "title", "body"), List.of(file));
        when(service.updatePost(eq(3L), eq(7L), any(), any())).thenReturn(new PostUpdateResponse(3L, 2L, "changed", "body", now));
        mvc.perform(multipart(HttpMethod.PATCH, "/api/posts/3").file(file).param("categoryId", "2").param("title", "changed").param("content", "body").with(authenticated()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.categoryId").value(2)).andExpect(jsonPath("$.title").value("changed"))
                .andExpect(jsonPath("$.content").value("body")).andExpect(jsonPath("$.updatedAt").exists());
        verify(service).updatePost(3L, 7L, new PostUpdateRequest(2L, "changed", "body"), List.of(file));
    }

    @Test void noAttachmentAndMaximumFieldLengthsAreAccepted() throws Exception {
        for (var method : List.of(HttpMethod.POST, HttpMethod.PATCH)) {
            mvc.perform(multipart(method, method == HttpMethod.POST ? "/api/posts" : "/api/posts/3")
                    .param("categoryId", "2").param("title", "t".repeat(100)).param("content", "c".repeat(5000)).with(authenticated()))
                    .andExpect(status().is(method == HttpMethod.POST ? 201 : 200));
        }
        verify(service).createPost(7L, new PostCreateRequest(2L, "t".repeat(100), "c".repeat(5000)), null);
        verify(service).updatePost(3L, 7L, new PostUpdateRequest(2L, "t".repeat(100), "c".repeat(5000)), null);
    }

    static Stream<String[]> invalidFields() {
        return Stream.of(new String[]{"", "title", "body"}, new String[]{"2", " ", "body"},
                new String[]{"2", "title", " "}, new String[]{"2", "t".repeat(101), "body"},
                new String[]{"2", "title", "c".repeat(5001)}, new String[]{"bad", "title", "body"});
    }

    @ParameterizedTest @MethodSource("invalidFields")
    void createAndUpdateRejectInvalidFields(String category, String title, String content) throws Exception {
        for (var method : List.of(HttpMethod.POST, HttpMethod.PATCH)) {
            mvc.perform(multipart(method, method == HttpMethod.POST ? "/api/posts" : "/api/posts/3")
                    .param("categoryId", category).param("title", title).param("content", content).with(authenticated()))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400)).andExpect(jsonPath("$.message").isNotEmpty());
        }
        verifyNoInteractions(service);
    }

    @Test void publicListMapsDefaultsFiltersAndPageDto() throws Exception {
        when(service.getPosts(null, null, 0, 10)).thenReturn(new PostPageResponse(List.of(), 0, 10, 0, 0, false, false, true, true));
        mvc.perform(get("/api/posts")).andExpect(status().isOk()).andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.currentPage").value(0)).andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.totalElements").value(0)).andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.hasNext").value(false)).andExpect(jsonPath("$.hasPrevious").value(false))
                .andExpect(jsonPath("$.isFirst").value(true)).andExpect(jsonPath("$.isLast").value(true));
        mvc.perform(get("/api/posts").param("keyword", "Spring").param("categoryId", "2").param("page", "1").param("size", "3"))
                .andExpect(status().isOk());
        verify(service).getPosts("Spring", 2L, 1, 3);
        mvc.perform(get("/api/posts").param("page", "bad")).andExpect(status().isBadRequest());
    }

    @Test void detailPassesOptionalPrincipalAndDeleteReturnsNoBody() throws Exception {
        when(service.getPost(3L, null)).thenReturn(new PostDetailResponse(3L, "nick", "Java", "title", "body", 2, false, LocalDateTime.now(), List.of()));
        mvc.perform(get("/api/posts/3")).andExpect(status().isOk()).andExpect(jsonPath("$.likedByMe").value(false))
                .andExpect(jsonPath("$.likeCount").value(2)).andExpect(jsonPath("$.files").isEmpty());
        mvc.perform(get("/api/posts/3").with(authenticated())).andExpect(status().isOk());
        verify(service).getPost(3L, 7L);
        mvc.perform(delete("/api/posts/3").with(authenticated())).andExpect(status().isNoContent()).andExpect(content().string(""));
        verify(service).deletePost(3L, 7L);
    }

    @Test void businessFailuresKeepExistingErrorContract() throws Exception {
        for (var error : List.of(ErrorCode.POST_NOT_FOUND, ErrorCode.POST_AUTHOR_MISMATCH, ErrorCode.FILE_STORAGE_FAILED)) {
            doThrow(new CustomException(error)).when(service).deletePost(3L, 7L);
            mvc.perform(delete("/api/posts/3").with(authenticated())).andExpect(status().is(error.getStatus()))
                    .andExpect(jsonPath("$.status").value(error.getStatus())).andExpect(jsonPath("$.message").value(error.getMessage()));
        }
    }
}
