package com.github.marcel615.askteacher.domain.comment.repository;

import com.github.marcel615.askteacher.domain.comment.entity.Comment;
import com.github.marcel615.askteacher.domain.commentlike.entity.CommentLike;
import com.github.marcel615.askteacher.support.RepositoryTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class CommentRepositoryTest extends RepositoryTestSupport {
    @Autowired CommentRepository comments;

    @Test void latestPageExcludesDeletedAndMapsLikeCountAndCurrentUser() {
        var alice = user("alice");
        var bob = user("bob");
        var post = post(alice, category("Java"), "title", "body");
        var old = comment(post, alice, "old", LocalDateTime.now().minusDays(1));
        var recent = comment(post, bob, "recent", LocalDateTime.now());
        var deleted = comment(post, alice, "deleted", LocalDateTime.now().plusDays(1));
        deleted.delete();
        em.persist(CommentLike.create(recent, alice));
        em.persist(CommentLike.create(recent, bob));
        flushAndClear();

        var page = comments.findLatestPage(post.getId(), alice.getId(), PageRequest.of(0, 1));
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getContent()).singleElement().satisfies(row -> {
            assertThat(row.commentId()).isEqualTo(recent.getId());
            assertThat(row.likeCount()).isEqualTo(2);
            assertThat(row.likedByMe()).isTrue();
        });
        assertThat(comments.findLatestPage(post.getId(), null, PageRequest.of(0, 10)).getContent())
                .allMatch(row -> !row.likedByMe());
        assertThat(comments.findByIdAndDeletedFalse(deleted.getId())).isEmpty();
        assertThat(comments.findByIdAndDeletedFalse(old.getId())).isPresent();
    }

    @Test void likeCountSortUsesCreatedAtAndIdAsTieBreakers() {
        var user = user("alice");
        var bob = user("bob");
        var post = post(user, category("Java"), "title", "body");
        LocalDateTime sameTime = LocalDateTime.now();
        var first = comment(post, user, "first", sameTime);
        var second = comment(post, user, "second", sameTime);
        var popular = comment(post, user, "popular", sameTime.minusDays(1));
        em.persist(CommentLike.create(popular, user));
        em.persist(CommentLike.create(popular, bob));
        flushAndClear();
        assertThat(comments.findLikeCountPage(post.getId(), null, PageRequest.of(0, 10)).getContent())
                .extracting(row -> row.commentId())
                .containsExactly(popular.getId(), second.getId(), first.getId());
    }

    private Comment comment(com.github.marcel615.askteacher.domain.post.entity.Post post,
                            com.github.marcel615.askteacher.domain.user.entity.User user,
                            String content, LocalDateTime createdAt) {
        Comment comment = Comment.create(post, user, content);
        ReflectionTestUtils.setField(comment, "createdAt", createdAt);
        ReflectionTestUtils.setField(comment, "updatedAt", createdAt);
        em.persist(comment);
        return comment;
    }
}
