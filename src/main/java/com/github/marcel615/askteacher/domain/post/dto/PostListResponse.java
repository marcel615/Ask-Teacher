package com.github.marcel615.askteacher.domain.post.dto;

import com.github.marcel615.askteacher.domain.post.entity.Post;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class PostListResponse {

    private Long postId;
    private String title;
    private String writerNickname;
    private boolean newPost;
    private LocalDateTime createdAt;

    private PostListResponse(Long postId, String title, String writerNickname, boolean newPost, LocalDateTime createdAt) {
        this.postId = postId;
        this.title = title;
        this.writerNickname = writerNickname;
        this.newPost = newPost;
        this.createdAt = createdAt;
    }

    public static PostListResponse from(Post post) {
        return new PostListResponse(
                post.getId(),
                post.getTitle(),
                post.getUser().getNickname(),
                post.isNewPost(),
                post.getCreatedAt()
        );
    }
}
