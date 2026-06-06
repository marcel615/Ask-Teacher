package com.github.marcel615.askteacher.domain.post.service;

import com.github.marcel615.askteacher.domain.category.entity.Category;
import com.github.marcel615.askteacher.domain.category.repository.CategoryRepository;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class PostServiceTest {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void getPostReturnsPostDetail() {
        User user = userRepository.save(User.createUser("post-detail@example.com", "password", "postDetailUser"));
        Category category = categoryRepository.save(Category.createCategory("post-detail-category"));
        Post post = postRepository.save(Post.createPost(user, category, "detail title", "detail content"));

        PostDetailResponse response = postService.getPost(post.getId());

        assertThat(response.getUserName()).isEqualTo("postDetailUser");
        assertThat(response.getCategoryName()).isEqualTo("post-detail-category");
        assertThat(response.getTitle()).isEqualTo("detail title");
        assertThat(response.getContent()).isEqualTo("detail content");
        assertThat(response.getCreatedAt()).isNotNull();
    }

    @Test
    void getPostThrowsWhenPostDoesNotExist() {
        assertThatThrownBy(() -> postService.getPost(999999L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.POST_NOT_FOUND);
    }

    @Test
    void getPostsReturnsOnlyNotDeletedPosts() {
        User user = userRepository.save(User.createUser("post-list@example.com", "password", "postListUser"));
        Category category = categoryRepository.save(Category.createCategory("post-list-category"));

        Post visiblePost = postRepository.save(Post.createPost(user, category, "visible title", "visible content"));
        Post deletedPost = Post.createPost(user, category, "deleted title", "deleted content");
        ReflectionTestUtils.setField(deletedPost, "deleted", true);
        postRepository.save(deletedPost);

        List<PostListResponse> responses = postService.getPosts();

        assertThat(responses)
                .extracting(PostListResponse::getPostId)
                .contains(visiblePost.getId())
                .doesNotContain(deletedPost.getId());

        PostListResponse response = responses.stream()
                .filter(post -> post.getPostId().equals(visiblePost.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(response.getTitle()).isEqualTo("visible title");
        assertThat(response.getWriterNickname()).isEqualTo("postListUser");
        assertThat(response.isNewPost()).isTrue();
    }

    @Test
    void updatePostUpdatesCategoryTitleAndContent() {
        User user = userRepository.save(User.createUser("post-update@example.com", "password", "postUpdateUser"));
        Category originalCategory = categoryRepository.save(Category.createCategory("original-category"));
        Category newCategory = categoryRepository.save(Category.createCategory("new-category"));
        Post post = postRepository.save(Post.createPost(user, originalCategory, "original title", "original content"));

        PostUpdateRequest request = createUpdateRequest(
                user.getId(),
                newCategory.getId(),
                "updated title",
                "updated content"
        );

        PostUpdateResponse response = postService.updatePost(post.getId(), request);

        assertThat(response.getPostId()).isEqualTo(post.getId());
        assertThat(response.getCategoryId()).isEqualTo(newCategory.getId());
        assertThat(response.getTitle()).isEqualTo("updated title");
        assertThat(response.getContent()).isEqualTo("updated content");
        assertThat(response.getUpdatedAt()).isNotNull();

        Post updatedPost = postRepository.findById(post.getId()).orElseThrow();
        assertThat(updatedPost.getCategory().getId()).isEqualTo(newCategory.getId());
        assertThat(updatedPost.getTitle()).isEqualTo("updated title");
        assertThat(updatedPost.getContent()).isEqualTo("updated content");
    }

    @Test
    void updatePostThrowsForbiddenWhenUserIsNotAuthor() {
        User author = userRepository.save(User.createUser("post-author@example.com", "password", "postAuthor"));
        User anotherUser = userRepository.save(User.createUser("post-another@example.com", "password", "postAnother"));
        Category category = categoryRepository.save(Category.createCategory("author-check-category"));
        Post post = postRepository.save(Post.createPost(author, category, "title", "content"));
        PostUpdateRequest request = createUpdateRequest(
                anotherUser.getId(),
                category.getId(),
                "updated title",
                "updated content"
        );

        assertThatThrownBy(() -> postService.updatePost(post.getId(), request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.POST_AUTHOR_MISMATCH);
    }

    @Test
    void updatePostThrowsWhenPostDoesNotExist() {
        User user = userRepository.save(User.createUser("post-missing@example.com", "password", "postMissingUser"));
        Category category = categoryRepository.save(Category.createCategory("post-missing-category"));
        PostUpdateRequest request = createUpdateRequest(
                user.getId(),
                category.getId(),
                "updated title",
                "updated content"
        );

        assertThatThrownBy(() -> postService.updatePost(999999L, request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.POST_NOT_FOUND);
    }

    @Test
    void updatePostThrowsWhenUserDoesNotExist() {
        User author = userRepository.save(User.createUser("post-user-missing@example.com", "password", "postUserMissing"));
        Category category = categoryRepository.save(Category.createCategory("user-missing-category"));
        Post post = postRepository.save(Post.createPost(author, category, "title", "content"));
        PostUpdateRequest request = createUpdateRequest(
                999999L,
                category.getId(),
                "updated title",
                "updated content"
        );

        assertThatThrownBy(() -> postService.updatePost(post.getId(), request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void updatePostThrowsWhenCategoryDoesNotExist() {
        User user = userRepository.save(User.createUser("post-category-missing@example.com", "password", "postCategoryMissing"));
        Category category = categoryRepository.save(Category.createCategory("category-missing-original"));
        Post post = postRepository.save(Post.createPost(user, category, "title", "content"));
        PostUpdateRequest request = createUpdateRequest(
                user.getId(),
                999999L,
                "updated title",
                "updated content"
        );

        assertThatThrownBy(() -> postService.updatePost(post.getId(), request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CATEGORY_NOT_FOUND);
    }

    @Test
    void deletePostSoftDeletesPostAndUpdatesUpdatedAt() {
        User user = userRepository.save(User.createUser("post-delete@example.com", "password", "postDeleteUser"));
        Category category = categoryRepository.save(Category.createCategory("post-delete-category"));
        Post post = Post.createPost(user, category, "delete title", "delete content");
        LocalDateTime oldUpdatedAt = LocalDateTime.now().minusDays(1);
        ReflectionTestUtils.setField(post, "updatedAt", oldUpdatedAt);
        postRepository.save(post);

        postService.deletePost(post.getId());

        Post deletedPost = postRepository.findById(post.getId()).orElseThrow();
        assertThat(deletedPost.isDeleted()).isTrue();
        assertThat(deletedPost.getUpdatedAt()).isAfter(oldUpdatedAt);
    }

    @Test
    void deletePostThrowsWhenPostDoesNotExist() {
        assertThatThrownBy(() -> postService.deletePost(999999L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.POST_NOT_FOUND);
    }

    @Test
    void deletePostThrowsWhenPostAlreadyDeleted() {
        User user = userRepository.save(User.createUser("post-already-deleted@example.com", "password", "postAlreadyDeletedUser"));
        Category category = categoryRepository.save(Category.createCategory("post-already-deleted-category"));
        Post post = Post.createPost(user, category, "already deleted title", "already deleted content");
        ReflectionTestUtils.setField(post, "deleted", true);
        postRepository.save(post);

        assertThatThrownBy(() -> postService.deletePost(post.getId()))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.POST_NOT_FOUND);
    }

    private PostUpdateRequest createUpdateRequest(Long userId, Long categoryId, String title, String content) {
        PostUpdateRequest request = new PostUpdateRequest();
        ReflectionTestUtils.setField(request, "userId", userId);
        ReflectionTestUtils.setField(request, "categoryId", categoryId);
        ReflectionTestUtils.setField(request, "title", title);
        ReflectionTestUtils.setField(request, "content", content);
        return request;
    }
}
