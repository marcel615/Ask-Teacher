package com.github.marcel615.askteacher.domain.commentlike.repository;

import com.github.marcel615.askteacher.domain.comment.entity.Comment;
import com.github.marcel615.askteacher.domain.commentlike.entity.CommentLike;
import com.github.marcel615.askteacher.support.RepositoryTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.*;

class CommentLikeRepositoryTest extends RepositoryTestSupport {
    @org.junit.jupiter.api.Nested
    @org.junit.jupiter.api.DisplayName("댓글 좋아요 저장과 삭제")
    class Scenarios {
        @Autowired CommentLikeRepository likes;

        @org.junit.jupiter.api.DisplayName("댓글 좋아요 조합의 유일성을 보장하고 댓글의 좋아요를 삭제한다")
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

        @org.junit.jupiter.api.DisplayName("같은 사용자는 같은 댓글에 중복 좋아요를 할 수 없다")
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
}
