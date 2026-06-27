package com.github.marcel615.askteacher.domain.postlike.service;

import com.github.marcel615.askteacher.domain.post.entity.Post;
import com.github.marcel615.askteacher.domain.post.repository.PostRepository;
import com.github.marcel615.askteacher.domain.postlike.entity.PostLike;
import com.github.marcel615.askteacher.domain.postlike.repository.PostLikeRepository;
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
public class PostLikeService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;

    @Transactional
    public void likePost(Long postId, Long userId) {
        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (postLikeRepository.existsByPostAndUser(post, user)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        try {
            postLikeRepository.saveAndFlush(PostLike.create(post, user));
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        postRepository.increaseLikeCount(postId);
    }

    @Transactional
    public void unlikePost(Long postId, Long userId) {
        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        PostLike postLike = postLikeRepository.findByPostAndUser(post, user)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE));

        postLikeRepository.delete(postLike);
        postRepository.decreaseLikeCount(postId);
    }
}
