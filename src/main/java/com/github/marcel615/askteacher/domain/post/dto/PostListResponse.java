package com.github.marcel615.askteacher.domain.post.dto;

import com.github.marcel615.askteacher.domain.post.entity.Post;

import java.time.LocalDateTime;

public record PostListResponse(
        Long postId,
        String title,
        String writerNickname,
        String categoryName,
        boolean newPost,
        LocalDateTime createdAt
) {

    public static PostListResponse from(Post post) {
        return new PostListResponse(
                post.getId(),
                post.getTitle(),
                post.getUser().getNickname(),
                post.getCategory().getName(),
                post.isNewPost(),
                post.getCreatedAt()
        );
    }
}
