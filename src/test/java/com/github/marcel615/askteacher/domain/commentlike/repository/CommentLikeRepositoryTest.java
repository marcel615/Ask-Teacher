package com.github.marcel615.askteacher.domain.commentlike.repository;

import com.github.marcel615.askteacher.domain.comment.entity.Comment;
import com.github.marcel615.askteacher.domain.commentlike.entity.CommentLike;
import com.github.marcel615.askteacher.support.RepositoryTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.*;

class CommentLikeRepositoryTest extends RepositoryTestSupport {
    @Autowired CommentLikeRepository likes;

    @Test void enforcesUniquePairAndDeletesAllLikesForComment() {
        var alice = user("alice");
        var bob = user("bob");
        var post = post(alice, category("Java"), "title", "body");
        var comment = Comment.create(post, alice, "comment");
        em.persist(comment);
        likes.saveAndFlush(CommentLike.create(comment, alice));
        likes.saveAndFlush(CommentLike.create(comment, bob));
        assertThat(likes.countByCommentId(comment.getId())).isEqualTo(2);
        assertThat(likes.existsByCommentIdAndUserId(comment.getId(), alice.getId())).isTrue();
        assertThat(likes.findByCommentIdAndUserId(comment.getId(), bob.getId())).isPresent();
        assertThat(likes.deleteByCommentId(comment.getId())).isEqualTo(2);
        flushAndClear();
        assertThat(likes.countByCommentId(comment.getId())).isZero();
    }

    @Test void sameUserCannotLikeSameCommentTwice() {
        var user = user("alice");
        var post = post(user, category("Java"), "title", "body");
        var comment = Comment.create(post, user, "comment");
        em.persist(comment);
        likes.saveAndFlush(CommentLike.create(comment, user));
        assertThatThrownBy(() -> likes.saveAndFlush(CommentLike.create(comment, user)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
