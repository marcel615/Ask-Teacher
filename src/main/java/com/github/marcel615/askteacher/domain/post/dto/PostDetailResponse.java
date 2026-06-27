package com.github.marcel615.askteacher.domain.post.dto;

import com.github.marcel615.askteacher.domain.post.entity.Post;

import java.time.LocalDateTime;

public record PostDetailResponse(
        Long postId,
        String userName,
        String categoryName,
        String title,
        String content,
        long likeCount,
        boolean likedByMe,
        LocalDateTime createdAt
) {

    public static PostDetailResponse from(Post post, boolean likedByMe) {
        return new PostDetailResponse(
                post.getId(),
                post.getUser().getNickname(),
                post.getCategory().getName(),
                post.getTitle(),
                post.getContent(),
                post.getLikeCount(),
                likedByMe,
                post.getCreatedAt()
        );
    }
}
