package com.github.marcel615.askteacher.domain.commentlike.dto;

public record CommentLikeResponse(
        Long commentId,
        long likeCount,
        boolean likedByMe
) {
}
