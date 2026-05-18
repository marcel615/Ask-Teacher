package com.github.marcel615.askteacher.domain.post.dto;

import com.github.marcel615.askteacher.domain.post.entity.Post;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class PostCreateResponse {

    private Long postId;
    private String title;
    private boolean newPost;
    private LocalDateTime createdAt;

    private PostCreateResponse(Long postId, String title, boolean newPost, LocalDateTime createdAt) {
        this.postId = postId;
        this.title = title;
        this.newPost = newPost;
        this.createdAt = createdAt;
    }

    public static PostCreateResponse from(Post post) {
        return new PostCreateResponse(
                post.getId(),
                post.getTitle(),
                post.isNewPost(),
                post.getCreatedAt()
        );
    }

}
