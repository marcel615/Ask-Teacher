package com.github.marcel615.askteacher.domain.post.controller;

import com.github.marcel615.askteacher.domain.post.dto.PostCreateRequest;
import com.github.marcel615.askteacher.domain.post.dto.PostCreateResponse;
import com.github.marcel615.askteacher.domain.post.dto.PostDetailResponse;
import com.github.marcel615.askteacher.domain.post.dto.PostListResponse;
import com.github.marcel615.askteacher.domain.post.dto.PostUpdateRequest;
import com.github.marcel615.askteacher.domain.post.dto.PostUpdateResponse;
import com.github.marcel615.askteacher.domain.post.service.PostService;
import com.github.marcel615.askteacher.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PostCreateResponse> createPost(@Valid @RequestBody PostCreateRequest postCreateRequest) {
        PostCreateResponse postCreateResponse = postService.createPost(postCreateRequest);
        return ApiResponse.success(201, "게시글이 작성되었습니다.", postCreateResponse);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<PostListResponse>> getPosts() {
        List<PostListResponse> postListResponses = postService.getPosts();
        return ApiResponse.success(200, "게시글 목록 조회에 성공했습니다.", postListResponses);
    }

    @GetMapping("/{postId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<PostDetailResponse> getPost(@PathVariable Long postId) {
        PostDetailResponse postDetailResponse = postService.getPost(postId);
        return ApiResponse.success(200, "게시글 상세 조회에 성공했습니다.", postDetailResponse);
    }

    @PatchMapping("/{postId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<PostUpdateResponse> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequest postUpdateRequest
    ) {
        PostUpdateResponse postUpdateResponse = postService.updatePost(postId, postUpdateRequest);
        return ApiResponse.success(200, "게시글이 수정되었습니다.", postUpdateResponse);
    }

    @DeleteMapping("/{postId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
        return ApiResponse.success(200, "게시글이 삭제되었습니다.");
    }
}
