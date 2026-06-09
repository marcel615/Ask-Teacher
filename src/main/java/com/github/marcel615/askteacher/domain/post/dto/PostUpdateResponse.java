package com.github.marcel615.askteacher.domain.post.dto;

import com.github.marcel615.askteacher.domain.post.entity.Post;

import java.time.LocalDateTime;

public record PostUpdateResponse(
        Long postId,
        Long categoryId,
        String title,
        String content,
        LocalDateTime updatedAt
) {

    public static PostUpdateResponse from(Post post) {
        return new PostUpdateResponse(
                post.getId(),
                post.getCategory().getId(),
                post.getTitle(),
                post.getContent(),
                post.getUpdatedAt()
        );
    }
}
