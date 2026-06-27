package com.github.marcel615.askteacher.domain.postlike.controller;

import com.github.marcel615.askteacher.domain.postlike.service.PostLikeService;
import com.github.marcel615.askteacher.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts/{postId}/likes")
public class PostLikeController {

    private final PostLikeService postLikeService;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> likePost(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId
    ) {
        postLikeService.likePost(postId, userId);
        return ApiResponse.success(200, "Post liked.");
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> unlikePost(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId
    ) {
        postLikeService.unlikePost(postId, userId);
        return ApiResponse.success(200, "Post like canceled.");
    }
}
