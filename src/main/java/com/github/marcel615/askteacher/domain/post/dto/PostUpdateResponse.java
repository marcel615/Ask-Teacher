package com.github.marcel615.askteacher.domain.post.dto;

import com.github.marcel615.askteacher.domain.post.entity.Post;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class PostUpdateResponse {

    private Long postId;
    private Long categoryId;
    private String title;
    private String content;
    private LocalDateTime updatedAt;

    private PostUpdateResponse(Long postId, Long categoryId, String title, String content, LocalDateTime updatedAt) {
        this.postId = postId;
        this.categoryId = categoryId;
        this.title = title;
        this.content = content;
        this.updatedAt = updatedAt;
    }

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
