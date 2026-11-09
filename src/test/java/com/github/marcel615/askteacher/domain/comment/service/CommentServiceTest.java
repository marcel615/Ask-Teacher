package com.github.marcel615.askteacher.domain.comment.service;

import com.github.marcel615.askteacher.domain.category.entity.Category;
import com.github.marcel615.askteacher.domain.comment.dto.*;
import com.github.marcel615.askteacher.domain.comment.entity.Comment;
import com.github.marcel615.askteacher.domain.comment.repository.CommentRepository;
import com.github.marcel615.askteacher.domain.commentlike.repository.CommentLikeRepository;
import com.github.marcel615.askteacher.domain.post.entity.Post;
import com.github.marcel615.askteacher.domain.post.repository.PostRepository;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {
    @Nested
    @DisplayName("댓글 처리")
    class Scenarios {
        @Mock CommentRepository comments;
        @Mock CommentLikeRepository likes;
        @Mock PostRepository posts;
        @Mock UserRepository users;
        @InjectMocks CommentService service;
        User user;
        Post post;
        Comment comment;

        @BeforeEach void setUp() {
            user = User.createUser("a@example.com", "encoded", "alice");
            Category category = Category.createCategory("Java");
            ReflectionTestUtils.setField(user, "id", 1L);
            ReflectionTestUtils.setField(category, "id", 2L);
            post = Post.createPost(user, category, "title", "body");
            ReflectionTestUtils.setField(post, "id", 3L);
            comment = Comment.create(post, user, "original");
            ReflectionTestUtils.setField(comment, "id", 4L);
            LocalDateTime auditDate = LocalDateTime.now().minusDays(1);
            ReflectionTestUtils.setField(comment, "createdAt", auditDate);
            ReflectionTestUtils.setField(comment, "updatedAt", auditDate);
        }

        @DisplayName("댓글 생성 시 내용을 정리하고 응답을 초기화한다")
        @Test void createTrimsContentAndInitializesResponse() {
            when(posts.findByIdAndDeletedFalse(3L)).thenReturn(Optional.of(post));
            when(users.findById(1L)).thenReturn(Optional.of(user));
            when(comments.save(any())).thenAnswer(call -> call.getArgument(0));
            var result = service.createComment(3L, 1L, new CommentCreateRequest("  content  "));
            assertThat(result.content()).isEqualTo("content");
            assertThat(result.likeCount()).isZero();
            assertThat(result.likedByMe()).isFalse();
        }

        @DisplayName("댓글 목록은 게시글과 페이지를 검증하고 정렬 쿼리를 선택한다")
        @Test void listValidatesPostPagingAndSelectsSortQuery() {
            when(posts.findByIdAndDeletedFalse(3L)).thenReturn(Optional.of(post));
            var row = new CommentResponse(4L, 3L, 1L, "alice", "body", 2, true, LocalDateTime.now(), LocalDateTime.now());
            var page = new PageImpl<>(List.of(row), PageRequest.of(0, 20), 1);
            when(comments.findLatestPage(3L, null, PageRequest.of(0, 20))).thenReturn(page);
            when(comments.findLikeCountPage(3L, 1L, PageRequest.of(0, 20))).thenReturn(page);
            assertThat(service.getComments(3L, null, 0, 20, "latest").sort()).isEqualTo("latest");
            assertThat(service.getComments(3L, 1L, 0, 20, "likeCount").comments()).containsExactly(row);
            for (var args : List.of(new Object[]{-1, 20, "latest"}, new Object[]{0, 0, "latest"},
                    new Object[]{0, 101, "latest"}, new Object[]{0, 20, "bad"})) {
                assertThatThrownBy(() -> service.getComments(3L, null, (int) args[0], (int) args[1], (String) args[2]))
                        .extracting("errorCode").isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
            }
            when(posts.findByIdAndDeletedFalse(9L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.getComments(9L, null, 0, 20, "latest"))
                    .extracting("errorCode").isEqualTo(ErrorCode.POST_NOT_FOUND);
        }

        @DisplayName("댓글 수정은 작성자를 확인하고 내용과 좋아요 상태를 반환한다")
        @Test void updateChecksAuthorTrimsAndIncludesLikeState() {
            when(comments.findByIdAndDeletedFalse(4L)).thenReturn(Optional.of(comment));
            when(likes.countByCommentId(4L)).thenReturn(2L);
            when(likes.existsByCommentIdAndUserId(4L, 1L)).thenReturn(true);
            var before = comment.getUpdatedAt();
            doAnswer(invocation -> {
                ReflectionTestUtils.setField(comment, "updatedAt", before.plusSeconds(1));
                return null;
            }).when(comments).flush();
            var result = service.updateComment(4L, 1L, new CommentUpdateRequest(" changed "));
            assertThat(result.content()).isEqualTo("changed");
            assertThat(result.likeCount()).isEqualTo(2);
            assertThat(result.likedByMe()).isTrue();
            assertThat(result.updatedAt()).isAfter(before);
            verify(comments).flush();
            assertThatThrownBy(() -> service.updateComment(4L, 9L, new CommentUpdateRequest("x")))
                    .extracting("errorCode").isEqualTo(ErrorCode.COMMENT_AUTHOR_MISMATCH);
        }

        @DisplayName("댓글 삭제는 좋아요를 제거하고 논리 삭제하며 없는 댓글을 거부한다")
        @Test void deleteRemovesLikesThenSoftDeletesAndRejectsMissing() {
            when(comments.findByIdAndDeletedFalse(4L)).thenReturn(Optional.of(comment));
            service.deleteComment(4L, 1L);
            verify(likes).deleteByCommentId(4L);
            assertThat(comment.isDeleted()).isTrue();
            assertThatThrownBy(() -> service.deleteComment(9L, 1L))
                    .extracting("errorCode").isEqualTo(ErrorCode.COMMENT_NOT_FOUND);
        }
    }
}
