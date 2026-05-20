package com.github.marcel615.askteacher.domain.post.service;

import com.github.marcel615.askteacher.domain.category.entity.Category;
import com.github.marcel615.askteacher.domain.category.repository.CategoryRepository;
import com.github.marcel615.askteacher.domain.post.dto.PostListResponse;
import com.github.marcel615.askteacher.domain.post.entity.Post;
import com.github.marcel615.askteacher.domain.post.repository.PostRepository;
import com.github.marcel615.askteacher.domain.user.entity.User;
import com.github.marcel615.askteacher.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(response.isNew()).isTrue();
    }
}
