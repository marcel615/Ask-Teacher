package com.github.marcel615.askteacher.domain.commentlike.service;

import com.github.marcel615.askteacher.domain.comment.entity.Comment;
import com.github.marcel615.askteacher.domain.comment.repository.CommentRepository;
import com.github.marcel615.askteacher.domain.commentlike.dto.CommentLikeResponse;
import com.github.marcel615.askteacher.domain.commentlike.entity.CommentLike;
import com.github.marcel615.askteacher.domain.commentlike.repository.CommentLikeRepository;
import com.github.marcel615.askteacher.domain.user.entity.User;
import com.github.marcel615.askteacher.domain.user.repository.UserRepository;
import com.github.marcel615.askteacher.global.exception.CustomException;
import com.github.marcel615.askteacher.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentLikeService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final UserRepository userRepository;

    @Transactional
    public CommentLikeResponse likeComment(Long commentId, Long userId) {
        Comment comment = getActiveComment(commentId);
        User user = getUser(userId);
        if (commentLikeRepository.existsByCommentIdAndUserId(commentId, userId)) {
            throw new CustomException(ErrorCode.DUPLICATE_COMMENT_LIKE);
        }

        try {
            commentLikeRepository.saveAndFlush(CommentLike.create(comment, user));
        } catch (DataIntegrityViolationException exception) {
            throw new CustomException(ErrorCode.DUPLICATE_COMMENT_LIKE);
        }
        return new CommentLikeResponse(commentId, commentLikeRepository.countByCommentId(commentId), true);
    }

    @Transactional
    public CommentLikeResponse unlikeComment(Long commentId, Long userId) {
        getActiveComment(commentId);
        getUser(userId);
        CommentLike commentLike = commentLikeRepository.findByCommentIdAndUserId(commentId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_LIKE_NOT_FOUND));
        commentLikeRepository.delete(commentLike);
        commentLikeRepository.flush();
        return new CommentLikeResponse(commentId, commentLikeRepository.countByCommentId(commentId), false);
    }

    private Comment getActiveComment(Long commentId) {
        return commentRepository.findByIdAndDeletedFalse(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
