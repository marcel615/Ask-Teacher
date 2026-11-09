package com.github.marcel615.askteacher.domain.commentlike.service;

import com.github.marcel615.askteacher.domain.comment.entity.Comment;
import com.github.marcel615.askteacher.domain.comment.repository.CommentRepository;
import com.github.marcel615.askteacher.domain.commentlike.entity.CommentLike;
import com.github.marcel615.askteacher.domain.commentlike.repository.CommentLikeRepository;
import com.github.marcel615.askteacher.domain.post.entity.Post;
import com.github.marcel615.askteacher.domain.user.entity.User;
import com.github.marcel615.askteacher.domain.user.repository.UserRepository;
import com.github.marcel615.askteacher.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentLikeServiceTest {
    @Nested
    @DisplayName("댓글 좋아요 처리")
    class Scenarios {
        @Mock CommentRepository comments;
        @Mock CommentLikeRepository likes;
        @Mock UserRepository users;
        @InjectMocks CommentLikeService service;
        User user;
        Comment comment;

        @BeforeEach void setUp() {
            user = User.createUser("a@example.com", "encoded", "alice");
            ReflectionTestUtils.setField(user, "id", 1L);
            comment = Comment.create(mock(Post.class), user, "body");
            ReflectionTestUtils.setField(comment, "id", 4L);
        }

        private void existing() {
            when(comments.findByIdAndDeletedFalse(4L)).thenReturn(Optional.of(comment));
            when(users.findById(1L)).thenReturn(Optional.of(user));
        }

        @DisplayName("댓글 좋아요를 저장하고 현재 개수를 반환한다")
        @Test void likeSavesAndReturnsCurrentCount() {
            existing();
            when(likes.countByCommentId(4L)).thenReturn(3L);
            var result = service.likeComment(4L, 1L);
            verify(likes).saveAndFlush(argThat(like -> like.getComment() == comment && like.getUser() == user));
            assertThat(result.likeCount()).isEqualTo(3);
            assertThat(result.likedByMe()).isTrue();
        }

        @DisplayName("중복 좋아요와 제약 조건 충돌을 충돌 오류로 변환한다")
        @Test void duplicateAndConstraintRaceMapToConflict() {
            existing();
            when(likes.existsByCommentIdAndUserId(4L, 1L)).thenReturn(true, false);
            assertThatThrownBy(() -> service.likeComment(4L, 1L)).extracting("errorCode")
                    .isEqualTo(ErrorCode.DUPLICATE_COMMENT_LIKE);
            when(likes.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("duplicate"));
            assertThatThrownBy(() -> service.likeComment(4L, 1L)).extracting("errorCode")
                    .isEqualTo(ErrorCode.DUPLICATE_COMMENT_LIKE);
        }

        @DisplayName("댓글 좋아요 취소를 반영하고 감소한 개수를 반환한다")
        @Test void unlikeDeletesFlushesAndReturnsReducedCount() {
            existing();
            CommentLike like = CommentLike.create(comment, user);
            when(likes.findByCommentIdAndUserId(4L, 1L)).thenReturn(Optional.of(like));
            when(likes.countByCommentId(4L)).thenReturn(1L);
            var result = service.unlikeComment(4L, 1L);
            var order = inOrder(likes);
            order.verify(likes).delete(like);
            order.verify(likes).flush();
            assertThat(result.likeCount()).isEqualTo(1);
            assertThat(result.likedByMe()).isFalse();
        }

        @DisplayName("대상이나 좋아요가 없으면 정의된 오류를 반환한다")
        @Test void missingResourcesAndMissingLikeUseDefinedErrors() {
            assertThatThrownBy(() -> service.likeComment(4L, 1L)).extracting("errorCode")
                    .isEqualTo(ErrorCode.COMMENT_NOT_FOUND);
            when(comments.findByIdAndDeletedFalse(4L)).thenReturn(Optional.of(comment));
            assertThatThrownBy(() -> service.likeComment(4L, 1L)).extracting("errorCode")
                    .isEqualTo(ErrorCode.USER_NOT_FOUND);
            when(users.findById(1L)).thenReturn(Optional.of(user));
            assertThatThrownBy(() -> service.unlikeComment(4L, 1L)).extracting("errorCode")
                    .isEqualTo(ErrorCode.COMMENT_LIKE_NOT_FOUND);
        }
    }
}
