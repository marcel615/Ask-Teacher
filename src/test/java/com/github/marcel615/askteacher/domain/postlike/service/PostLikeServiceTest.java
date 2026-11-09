package com.github.marcel615.askteacher.domain.postlike.service;

import com.github.marcel615.askteacher.domain.post.entity.Post;
import com.github.marcel615.askteacher.domain.post.repository.PostRepository;
import com.github.marcel615.askteacher.domain.postlike.entity.PostLike;
import com.github.marcel615.askteacher.domain.postlike.repository.PostLikeRepository;
import com.github.marcel615.askteacher.domain.user.entity.User;
import com.github.marcel615.askteacher.domain.user.repository.UserRepository;
import com.github.marcel615.askteacher.global.exception.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostLikeServiceTest {
    @org.junit.jupiter.api.Nested
    @org.junit.jupiter.api.DisplayName("게시글 좋아요 처리")
    class Scenarios {
        @Mock PostRepository posts;
        @Mock UserRepository users;
        @Mock PostLikeRepository likes;
        @InjectMocks PostLikeService service;
        private final User user = User.createUser("a@example.com", "encoded", "nick");
        private final Post post = Post.createPost(user, null, "title", "content");

        private void existing() {
            when(posts.findByIdAndDeletedFalse(10L)).thenReturn(Optional.of(post));
            when(users.findById(20L)).thenReturn(Optional.of(user));
        }

        @org.junit.jupiter.api.DisplayName("게시글 좋아요 관계를 저장한 후 개수를 증가시킨다")
        @Test void likeSavesRelationBeforeIncreasingCount() {
            existing();
            service.likePost(10L, 20L);
            InOrder order = inOrder(likes, posts);
            order.verify(likes).saveAndFlush(argThat(like -> like.getPost() == post && like.getUser() == user));
            order.verify(posts).increaseLikeCount(10L);
        }

        @org.junit.jupiter.api.DisplayName("중복 좋아요와 제약 조건 충돌은 개수를 증가시키지 않는다")
        @Test void duplicateLikeAndConstraintRaceDoNotIncreaseCount() {
            existing();
            when(likes.existsByPostAndUser(post, user)).thenReturn(true, false);
            assertThatThrownBy(() -> service.likePost(10L, 20L)).extracting("errorCode").isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
            when(likes.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("duplicate"));
            assertThatThrownBy(() -> service.likePost(10L, 20L)).extracting("errorCode").isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
            verify(posts, never()).increaseLikeCount(any());
        }

        @org.junit.jupiter.api.DisplayName("게시글 좋아요 취소는 관계를 삭제하고 개수를 줄인다")
        @Test void unlikeDeletesRelationAndDecreasesCount() {
            existing();
            PostLike like = PostLike.create(post, user);
            when(likes.findByPostAndUser(post, user)).thenReturn(Optional.of(like));
            service.unlikePost(10L, 20L);
            verify(likes).delete(like);
            verify(posts).decreaseLikeCount(10L);
        }

        @org.junit.jupiter.api.DisplayName("없는 좋아요를 취소해도 개수가 감소하지 않는다")
        @Test void missingLikeDoesNotDecreaseCount() {
            existing();
            assertThatThrownBy(() -> service.unlikePost(10L, 20L)).extracting("errorCode").isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
            verify(posts, never()).decreaseLikeCount(any());
        }

        @org.junit.jupiter.api.DisplayName("게시글이 없거나 삭제됐거나 사용자가 없으면 좋아요와 취소를 거부한다")
        @Test void missingOrDeletedPostAndMissingUserRejectBothActions() {
            assertThatThrownBy(() -> service.likePost(10L, 20L)).extracting("errorCode").isEqualTo(ErrorCode.POST_NOT_FOUND);
            assertThatThrownBy(() -> service.unlikePost(10L, 20L)).extracting("errorCode").isEqualTo(ErrorCode.POST_NOT_FOUND);
            when(posts.findByIdAndDeletedFalse(10L)).thenReturn(Optional.of(post));
            assertThatThrownBy(() -> service.likePost(10L, 20L)).extracting("errorCode").isEqualTo(ErrorCode.USER_NOT_FOUND);
            assertThatThrownBy(() -> service.unlikePost(10L, 20L)).extracting("errorCode").isEqualTo(ErrorCode.USER_NOT_FOUND);
            verifyNoInteractions(likes);
        }
    }
}
