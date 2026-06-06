package com.github.marcel615.askteacher.domain.post.dto;

import com.github.marcel615.askteacher.domain.post.entity.Post;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class PostDetailResponse {

    private String userName;
    private String categoryName;
    private String title;
    private String content;
    private LocalDateTime createdAt;

    private PostDetailResponse(String userName, String categoryName, String title, String content, LocalDateTime createdAt) {
        this.userName = userName;
        this.categoryName = categoryName;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
    }

    public static PostDetailResponse from(Post post) {
        return new PostDetailResponse(
                post.getUser().getNickname(),
                post.getCategory().getName(),
                post.getTitle(),
                post.getContent(),
                post.getCreatedAt()
        );
    }
}
