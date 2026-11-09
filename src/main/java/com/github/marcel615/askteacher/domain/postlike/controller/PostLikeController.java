package com.github.marcel615.askteacher.domain.postlike.controller;

import com.github.marcel615.askteacher.domain.postlike.service.PostLikeService;
import com.github.marcel615.askteacher.global.security.AskTeacherUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts/{postId}/likes")
public class PostLikeController {

    private final PostLikeService postLikeService;

    @PostMapping
    public ResponseEntity<Void> likePost(
            @AuthenticationPrincipal AskTeacherUserDetails principal,
            @PathVariable Long postId
    ) {
        Long userId = principal == null ? null : principal.id();
        postLikeService.likePost(postId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> unlikePost(
            @AuthenticationPrincipal AskTeacherUserDetails principal,
            @PathVariable Long postId
    ) {
        Long userId = principal == null ? null : principal.id();
        postLikeService.unlikePost(postId, userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
