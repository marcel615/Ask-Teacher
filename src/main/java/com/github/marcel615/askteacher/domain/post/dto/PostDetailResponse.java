package com.github.marcel615.askteacher.domain.post.dto;

import com.github.marcel615.askteacher.domain.post.entity.Post;
import com.github.marcel615.askteacher.domain.post.entity.PostFile;

import java.time.LocalDateTime;
import java.util.List;

public record PostDetailResponse(
        Long postId,
        String userName,
        String categoryName,
        String title,
        String content,
        long likeCount,
        boolean likedByMe,
        LocalDateTime createdAt,
        List<PostFileResponse> files
) {

    public static PostDetailResponse from(Post post, boolean likedByMe) {
        return from(post, likedByMe, List.of());
    }

    public static PostDetailResponse from(Post post, boolean likedByMe, List<PostFile> postFiles) {
        return new PostDetailResponse(
                post.getId(),
                post.getUser().getNickname(),
                post.getCategory().getName(),
                post.getTitle(),
                post.getContent(),
                post.getLikeCount(),
                likedByMe,
                post.getCreatedAt(),
                postFiles.stream()
                        .map(PostFileResponse::from)
                        .toList()
        );
    }
}
