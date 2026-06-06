package com.github.marcel615.askteacher.domain.post.service;

import com.github.marcel615.askteacher.domain.category.entity.Category;
import com.github.marcel615.askteacher.domain.category.repository.CategoryRepository;
import com.github.marcel615.askteacher.domain.post.dto.PostCreateRequest;
import com.github.marcel615.askteacher.domain.post.dto.PostCreateResponse;
import com.github.marcel615.askteacher.domain.post.dto.PostDetailResponse;
import com.github.marcel615.askteacher.domain.post.dto.PostListResponse;
import com.github.marcel615.askteacher.domain.post.dto.PostUpdateRequest;
import com.github.marcel615.askteacher.domain.post.dto.PostUpdateResponse;
import com.github.marcel615.askteacher.domain.post.entity.Post;
import com.github.marcel615.askteacher.domain.post.repository.PostRepository;
import com.github.marcel615.askteacher.domain.user.entity.User;
import com.github.marcel615.askteacher.domain.user.repository.UserRepository;
import com.github.marcel615.askteacher.global.exception.CustomException;
import com.github.marcel615.askteacher.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public PostCreateResponse createPost(PostCreateRequest postCreateRequest) {
        User user = userRepository.findById(postCreateRequest.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Category category = categoryRepository.findById(postCreateRequest.getCategoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        Post post = Post.createPost(user, category, postCreateRequest.getTitle(), postCreateRequest.getContent());

        Post savedPost = postRepository.save(post);

        return PostCreateResponse.from(savedPost);
    }

    @Transactional(readOnly = true)
    public List<PostListResponse> getPosts() {
        return postRepository.findByDeletedFalse().stream()
                .map(PostListResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PostDetailResponse getPost(Long postId) {
        Post post = postRepository.findWithUserAndCategoryById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        return PostDetailResponse.from(post);
    }

    @Transactional
    public PostUpdateResponse updatePost(Long postId, PostUpdateRequest postUpdateRequest) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        User user = userRepository.findById(postUpdateRequest.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Category category = categoryRepository.findById(postUpdateRequest.getCategoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        if (!post.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.POST_AUTHOR_MISMATCH);
        }

        post.update(category, postUpdateRequest.getTitle(), postUpdateRequest.getContent());

        return PostUpdateResponse.from(post);
    }
}
