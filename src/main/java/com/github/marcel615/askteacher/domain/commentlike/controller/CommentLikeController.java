package com.github.marcel615.askteacher.domain.commentlike.controller;

import com.github.marcel615.askteacher.domain.commentlike.dto.CommentLikeResponse;
import com.github.marcel615.askteacher.domain.commentlike.service.CommentLikeService;
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
@RequestMapping("/api/comments/{commentId}/likes")
public class CommentLikeController {

    private final CommentLikeService commentLikeService;

    @PostMapping
    public ResponseEntity<CommentLikeResponse> likeComment(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long commentId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentLikeService.likeComment(commentId, userId));
    }

    @DeleteMapping
    public ResponseEntity<CommentLikeResponse> unlikeComment(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long commentId
    ) {
        return ResponseEntity.ok(commentLikeService.unlikeComment(commentId, userId));
    }
}
