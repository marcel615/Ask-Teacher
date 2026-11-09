package com.github.marcel615.askteacher.domain.comment.controller;

import com.github.marcel615.askteacher.domain.comment.dto.CommentCreateRequest;
import com.github.marcel615.askteacher.domain.comment.dto.CommentPageResponse;
import com.github.marcel615.askteacher.domain.comment.dto.CommentResponse;
import com.github.marcel615.askteacher.domain.comment.dto.CommentUpdateRequest;
import com.github.marcel615.askteacher.domain.comment.service.CommentService;
import com.github.marcel615.askteacher.global.security.AskTeacherUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/api/posts/{postId}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @AuthenticationPrincipal AskTeacherUserDetails principal,
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequest request
    ) {
        Long userId = principal == null ? null : principal.id();
        CommentResponse response = commentService.createComment(postId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/posts/{postId}/comments")
    public ResponseEntity<CommentPageResponse> getComments(
            @AuthenticationPrincipal AskTeacherUserDetails principal,
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "latest") String sort
    ) {
        Long userId = principal == null ? null : principal.id();
        CommentPageResponse response = commentService.getComments(postId, userId, page, size, sort);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/api/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @AuthenticationPrincipal AskTeacherUserDetails principal,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateRequest request
    ) {
        Long userId = principal == null ? null : principal.id();
        CommentResponse response = commentService.updateComment(commentId, userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @AuthenticationPrincipal AskTeacherUserDetails principal,
            @PathVariable Long commentId
    ) {
        Long userId = principal == null ? null : principal.id();
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }
}
