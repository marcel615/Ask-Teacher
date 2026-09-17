package com.github.marcel615.askteacher.global.entity;

import com.github.marcel615.askteacher.domain.post.entity.Post;
import com.github.marcel615.askteacher.domain.postlike.entity.PostLike;
import com.github.marcel615.askteacher.support.RepositoryTestSupport;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class JpaAuditingRepositoryTest extends RepositoryTestSupport {

    @Test
    void baseEntityAutomaticallySetsIdAndCreatedAt() {
        var user = user("base-entity-user");
        var post = post(user, category("base-entity-category"), "title", "content");
        var postLike = PostLike.create(post, user);

        em.persist(postLike);
        flushAndClear();

        var saved = em.find(PostLike.class, postLike.getId());
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getPost().getId()).isEqualTo(post.getId());
        assertThat(saved.getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    void baseUpdatableEntityAutomaticallySetsAndUpdatesAuditDates() {
        var post = post(
                user("base-updatable-user"),
                category("base-updatable-category"),
                "title",
                "content"
        );
        flushAndClear();

        var saved = em.find(Post.class, post.getId());
        LocalDateTime createdAt = saved.getCreatedAt();
        LocalDateTime updatedAt = saved.getUpdatedAt();

        assertThat(saved.getId()).isNotNull();
        assertThat(createdAt).isNotNull();
        assertThat(updatedAt).isNotNull();

        saved.update(saved.getCategory(), "changed title", "changed content");
        em.flush();

        assertThat(saved.getCreatedAt()).isEqualTo(createdAt);
        assertThat(saved.getUpdatedAt()).isAfter(updatedAt);

        Long postId = saved.getId();
        flushAndClear();
        var reloaded = em.find(Post.class, postId);
        assertThat(reloaded.getCreatedAt()).isEqualTo(createdAt);
        assertThat(reloaded.getUpdatedAt()).isAfter(updatedAt);
    }
}
