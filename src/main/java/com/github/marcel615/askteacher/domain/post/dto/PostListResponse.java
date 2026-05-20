package com.github.marcel615.askteacher.domain.post.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("isNew")
    private boolean isNew;

    private LocalDateTime createdAt;

    private PostListResponse(Long postId, String title, String writerNickname, boolean isNew, LocalDateTime createdAt) {
        this.postId = postId;
        this.title = title;
        this.writerNickname = writerNickname;
        this.isNew = isNew;
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
