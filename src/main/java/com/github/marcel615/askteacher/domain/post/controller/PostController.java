package com.github.marcel615.askteacher.domain.post.controller;

import com.github.marcel615.askteacher.domain.post.dto.PostCreateRequest;
import com.github.marcel615.askteacher.domain.post.dto.PostCreateResponse;
import com.github.marcel615.askteacher.domain.post.dto.PostDetailResponse;
import com.github.marcel615.askteacher.domain.post.dto.PostPageResponse;
import com.github.marcel615.askteacher.domain.post.dto.PostUpdateRequest;
import com.github.marcel615.askteacher.domain.post.dto.PostUpdateResponse;
import com.github.marcel615.askteacher.domain.post.service.PostService;
import com.github.marcel615.askteacher.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PostCreateResponse> createPost(
            @AuthenticationPrincipal Long userId,
            @Valid @ModelAttribute PostCreateRequest postCreateRequest,
            @RequestParam(value = "files", required = false) List<MultipartFile> files
    ) {
        PostCreateResponse postCreateResponse = postService.createPost(userId, postCreateRequest, files);
        return ApiResponse.success(201, "게시글이 작성되었습니다.", postCreateResponse);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<PostPageResponse> getPosts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PostPageResponse postPageResponse = postService.getPosts(keyword, categoryId, page, size);
        return ApiResponse.success(200, "게시글 목록 조회에 성공했습니다.", postPageResponse);
    }

    @GetMapping("/{postId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<PostDetailResponse> getPost(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId
    ) {
        PostDetailResponse postDetailResponse = postService.getPost(postId, userId);
        return ApiResponse.success(200, "게시글 상세 조회에 성공했습니다.", postDetailResponse);
    }

    @PatchMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<PostUpdateResponse> updatePost(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId,
            @Valid @ModelAttribute PostUpdateRequest postUpdateRequest,
            @RequestParam(value = "files", required = false) List<MultipartFile> files
    ) {
        PostUpdateResponse postUpdateResponse = postService.updatePost(postId, userId, postUpdateRequest, files);
        return ApiResponse.success(200, "게시글이 수정되었습니다.", postUpdateResponse);
    }

    @DeleteMapping("/{postId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> deletePost(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId
    ) {
        postService.deletePost(postId, userId);
        return ApiResponse.success(200, "게시글이 삭제되었습니다.");
    }
}
