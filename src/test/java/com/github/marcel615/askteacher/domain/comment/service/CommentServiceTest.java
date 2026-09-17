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
    }

    @Test void createTrimsContentAndInitializesResponse() {
        when(posts.findByIdAndDeletedFalse(3L)).thenReturn(Optional.of(post));
        when(users.findById(1L)).thenReturn(Optional.of(user));
        when(comments.save(any())).thenAnswer(call -> call.getArgument(0));
        var result = service.createComment(3L, 1L, new CommentCreateRequest("  content  "));
        assertThat(result.content()).isEqualTo("content");
        assertThat(result.likeCount()).isZero();
        assertThat(result.likedByMe()).isFalse();
    }

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

    @Test void updateChecksAuthorTrimsAndIncludesLikeState() {
        when(comments.findByIdAndDeletedFalse(4L)).thenReturn(Optional.of(comment));
        when(likes.countByCommentId(4L)).thenReturn(2L);
        when(likes.existsByCommentIdAndUserId(4L, 1L)).thenReturn(true);
        var before = comment.getUpdatedAt();
        var result = service.updateComment(4L, 1L, new CommentUpdateRequest(" changed "));
        assertThat(result.content()).isEqualTo("changed");
        assertThat(result.likeCount()).isEqualTo(2);
        assertThat(result.likedByMe()).isTrue();
        assertThat(result.updatedAt()).isAfterOrEqualTo(before);
        assertThatThrownBy(() -> service.updateComment(4L, 9L, new CommentUpdateRequest("x")))
                .extracting("errorCode").isEqualTo(ErrorCode.COMMENT_AUTHOR_MISMATCH);
    }

    @Test void deleteRemovesLikesThenSoftDeletesAndRejectsMissing() {
        when(comments.findByIdAndDeletedFalse(4L)).thenReturn(Optional.of(comment));
        service.deleteComment(4L, 1L);
        verify(likes).deleteByCommentId(4L);
        assertThat(comment.isDeleted()).isTrue();
        assertThatThrownBy(() -> service.deleteComment(9L, 1L))
                .extracting("errorCode").isEqualTo(ErrorCode.COMMENT_NOT_FOUND);
    }
}
