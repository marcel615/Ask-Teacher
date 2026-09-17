package com.github.marcel615.askteacher.domain.post.entity;

import com.github.marcel615.askteacher.domain.category.entity.Category;
import com.github.marcel615.askteacher.domain.user.entity.User;
import com.github.marcel615.askteacher.global.entity.BaseUpdatableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "posts")
public class Post extends BaseUpdatableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private boolean newPost;

    @Column(nullable = false)
    private boolean deleted;

    @Column(nullable = false)
    private long likeCount;

    public static Post createPost(User user, Category category, String title, String content) {
        Post post = new Post();

        post.user = user;
        post.category = category;
        post.title = title;
        post.content = content;
        post.newPost = true;
        post.deleted = false;
        post.likeCount = 0L;

        return post;
    }

    public void update(Category category, String title, String content) {
        this.category = category;
        this.title = title;
        this.content = content;
    }

    public void delete() {
        this.deleted = true;
    }

}
