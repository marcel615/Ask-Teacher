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
import com.github.marcel615.askteacher.domain.post.entity.PostFile;
import com.github.marcel615.askteacher.domain.post.repository.PostFileRepository;
import com.github.marcel615.askteacher.domain.post.repository.PostRepository;
import com.github.marcel615.askteacher.domain.post.storage.PostFileStorage;
import com.github.marcel615.askteacher.domain.postlike.repository.PostLikeRepository;
import com.github.marcel615.askteacher.domain.user.entity.User;
import com.github.marcel615.askteacher.domain.user.repository.UserRepository;
import com.github.marcel615.askteacher.global.exception.CustomException;
import com.github.marcel615.askteacher.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final PostFileRepository postFileRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostFileStorage postFileStorage;

    @Transactional
    public PostCreateResponse createPost(Long userId, PostCreateRequest postCreateRequest) {
        return createPost(userId, postCreateRequest, List.of());
    }

    @Transactional
    public PostCreateResponse createPost(Long userId, PostCreateRequest postCreateRequest, List<MultipartFile> files) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Category category = categoryRepository.findById(postCreateRequest.categoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        Post post = Post.createPost(user, category, postCreateRequest.title(), postCreateRequest.content());

        Post savedPost = postRepository.save(post);
        List<PostFile> postFiles = postFileStorage.store(savedPost, files);
        postFileRepository.saveAll(postFiles);

        return PostCreateResponse.from(savedPost);
    }

    @Transactional(readOnly = true)
    public List<PostListResponse> getPosts() {
        return postRepository.findByDeletedFalseOrderByCreatedAtDesc().stream()
                .map(PostListResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PostDetailResponse getPost(Long postId) {
        return getPost(postId, null);
    }

    @Transactional(readOnly = true)
    public PostDetailResponse getPost(Long postId, Long userId) {
        Post post = postRepository.findWithUserAndCategoryByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        boolean likedByMe = userId != null && postLikeRepository.existsByPostIdAndUserId(postId, userId);
        List<PostFile> postFiles = postFileRepository.findByPostIdOrderByCreatedAtAsc(postId);

        return PostDetailResponse.from(post, likedByMe, postFiles);
    }

    @Transactional
    public PostUpdateResponse updatePost(Long postId, Long userId, PostUpdateRequest postUpdateRequest) {
        return updatePost(postId, userId, postUpdateRequest, List.of());
    }

    @Transactional
    public PostUpdateResponse updatePost(Long postId, Long userId, PostUpdateRequest postUpdateRequest, List<MultipartFile> files) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        validateAuthor(post, userId);

        Category category = categoryRepository.findById(postUpdateRequest.categoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        post.update(category, postUpdateRequest.title(), postUpdateRequest.content());
        List<PostFile> postFiles = postFileStorage.store(post, files);
        postFileRepository.saveAll(postFiles);

        return PostUpdateResponse.from(post);
    }

    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        validateAuthor(post, userId);

        post.delete();
    }

    private void validateAuthor(Post post, Long userId) {
        if (!post.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.POST_AUTHOR_MISMATCH);
        }
    }
}
