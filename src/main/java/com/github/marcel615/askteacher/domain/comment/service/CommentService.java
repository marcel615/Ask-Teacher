package com.github.marcel615.askteacher.domain.comment.service;

import com.github.marcel615.askteacher.domain.comment.dto.CommentCreateRequest;
import com.github.marcel615.askteacher.domain.comment.dto.CommentPageResponse;
import com.github.marcel615.askteacher.domain.comment.dto.CommentResponse;
import com.github.marcel615.askteacher.domain.comment.dto.CommentUpdateRequest;
import com.github.marcel615.askteacher.domain.comment.entity.Comment;
import com.github.marcel615.askteacher.domain.comment.repository.CommentRepository;
import com.github.marcel615.askteacher.domain.comment.type.CommentSort;
import com.github.marcel615.askteacher.domain.commentlike.repository.CommentLikeRepository;
import com.github.marcel615.askteacher.domain.post.entity.Post;
import com.github.marcel615.askteacher.domain.post.repository.PostRepository;
import com.github.marcel615.askteacher.domain.user.entity.User;
import com.github.marcel615.askteacher.domain.user.repository.UserRepository;
import com.github.marcel615.askteacher.global.exception.CustomException;
import com.github.marcel615.askteacher.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public CommentResponse createComment(Long postId, Long userId, CommentCreateRequest request) {
        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Comment comment = commentRepository.save(Comment.create(post, user, request.content().trim()));
        return CommentResponse.from(comment, 0L, false);
    }

    @Transactional(readOnly = true)
    public CommentPageResponse getComments(Long postId, Long userId, int page, int size, String sortValue) {
        if (page < 0 || size < 1 || size > 100) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (postRepository.findByIdAndDeletedFalse(postId).isEmpty()) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }

        CommentSort sort = CommentSort.from(sortValue);
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<CommentResponse> comments = sort == CommentSort.LATEST
                ? commentRepository.findLatestPage(postId, userId, pageRequest)
                : commentRepository.findLikeCountPage(postId, userId, pageRequest);
        return CommentPageResponse.from(comments, sort);
    }

    @Transactional
    public CommentResponse updateComment(Long commentId, Long userId, CommentUpdateRequest request) {
        Comment comment = getActiveComment(commentId);
        validateAuthor(comment, userId);
        comment.update(request.content().trim());
        long likeCount = commentLikeRepository.countByCommentId(commentId);
        boolean likedByMe = commentLikeRepository.existsByCommentIdAndUserId(commentId, userId);
        return CommentResponse.from(comment, likeCount, likedByMe);
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = getActiveComment(commentId);
        validateAuthor(comment, userId);
        commentLikeRepository.deleteByCommentId(commentId);
        comment.delete();
    }

    private Comment getActiveComment(Long commentId) {
        return commentRepository.findByIdAndDeletedFalse(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
    }

    private void validateAuthor(Comment comment, Long userId) {
        if (!comment.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.COMMENT_AUTHOR_MISMATCH);
        }
    }
}
