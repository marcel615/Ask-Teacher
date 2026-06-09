package com.github.marcel615.askteacher.domain.post.dto;

import com.github.marcel615.askteacher.domain.post.entity.Post;

import java.time.LocalDateTime;

public record PostCreateResponse(
        Long postId,
        String title,
        boolean newPost,
        LocalDateTime createdAt
) {

    public static PostCreateResponse from(Post post) {
        return new PostCreateResponse(
                post.getId(),
                post.getTitle(),
                post.isNewPost(),
                post.getCreatedAt()
        );
    }
}
