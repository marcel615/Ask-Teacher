package com.github.marcel615.askteacher.domain.post.service;

import com.github.marcel615.askteacher.domain.category.entity.Category;
import com.github.marcel615.askteacher.domain.category.repository.CategoryRepository;
import com.github.marcel615.askteacher.domain.post.dto.*;
import com.github.marcel615.askteacher.domain.post.entity.*;
import com.github.marcel615.askteacher.domain.post.repository.*;
import com.github.marcel615.askteacher.domain.post.storage.PostFileStorage;
import com.github.marcel615.askteacher.domain.postlike.repository.PostLikeRepository;
import com.github.marcel615.askteacher.domain.user.entity.User;
import com.github.marcel615.askteacher.domain.user.repository.UserRepository;
import com.github.marcel615.askteacher.global.exception.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.LocalDateTime;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {
    @Mock PostRepository posts;
    @Mock PostFileRepository files;
    @Mock UserRepository users;
    @Mock CategoryRepository categories;
    @Mock PostLikeRepository likes;
    @Mock PostFileStorage storage;
    @InjectMocks PostService service;
    User user;
    Category category;
    Post post;

    @BeforeEach void setUp() {
        user = User.createUser("a@example.com", "encoded", "nick");
        category = Category.createCategory("Java");
        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(category, "id", 2L);
        post = Post.createPost(user, category, "title", "content");
        ReflectionTestUtils.setField(post, "id", 3L);
    }

    @Test void createSavesInitialStateAndAttachments() {
        when(users.findById(1L)).thenReturn(Optional.of(user));
        when(categories.findById(2L)).thenReturn(Optional.of(category));
        when(posts.save(any())).thenAnswer(call -> {
            Post saved = call.getArgument(0);
            assertThat(saved.getUser()).isSameAs(user);
            assertThat(saved.getCategory()).isSameAs(category);
            assertThat(saved.getContent()).isEqualTo("content");
            assertThat(saved.isDeleted()).isFalse();
            assertThat(saved.getLikeCount()).isZero();
            ReflectionTestUtils.setField(saved, "id", 3L);
            return saved;
        });
        var upload = new MockMultipartFile("files", "a.png", "image/png", new byte[]{1});
        var stored = PostFile.create(post, "a.png", "stored.png", "path", "image/png", 1);
        when(storage.store(any(), eq(List.of(upload)))).thenReturn(List.of(stored));
        var result = service.createPost(1L, new PostCreateRequest(2L, "title", "content"), List.of(upload));
        assertThat(result.postId()).isEqualTo(3L);
        assertThat(result.newPost()).isTrue();
        assertThat(result.createdAt()).isNotNull();
        verify(files).saveAll(List.of(stored));
    }

    @Test void createRejectsMissingUserAndCategory() {
        var request = new PostCreateRequest(2L, "title", "content");
        assertThatThrownBy(() -> service.createPost(1L, request)).extracting("errorCode").isEqualTo(ErrorCode.USER_NOT_FOUND);
        when(users.findById(1L)).thenReturn(Optional.of(user));
        assertThatThrownBy(() -> service.createPost(1L, request)).extracting("errorCode").isEqualTo(ErrorCode.CATEGORY_NOT_FOUND);
        verifyNoInteractions(storage, files);
        verify(posts, never()).save(any());
    }

    @Test void searchNormalizesKeywordAndBuildsPage() {
        when(categories.existsById(2L)).thenReturn(true);
        when(posts.searchPosts(eq("Spring"), eq(2L), any())).thenReturn(new PageImpl<>(List.of(post), PageRequest.of(0, 1), 2));
        var result = service.getPosts("  Spring  ", 2L, 0, 1);
        assertThat(result.totalElements()).isEqualTo(2);
        assertThat(result.totalPages()).isEqualTo(2);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.content().get(0).postId()).isEqualTo(3L);
        verify(posts).searchPosts("Spring", 2L, PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @Test void blankSearchIsUnfiltered() {
        when(posts.searchPosts(isNull(), isNull(), any())).thenReturn(Page.empty());
        assertThat(service.getPosts("  ", null, 0, 10).content()).isEmpty();
        assertThat(service.getPosts(null, null, 0, 10).content()).isEmpty();
    }

    @Test void invalidPagingAndCategoryStopSearch() {
        assertThatThrownBy(() -> service.getPosts(null, null, -1, 10)).extracting("errorCode").isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
        assertThatThrownBy(() -> service.getPosts(null, null, 0, 0)).extracting("errorCode").isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
        assertThatThrownBy(() -> service.getPosts(null, 2L, 0, 10)).extracting("errorCode").isEqualTo(ErrorCode.CATEGORY_NOT_FOUND);
        verifyNoInteractions(posts);
    }

    @Test void detailMapsFilesAndCurrentUsersLike() {
        when(posts.findWithUserAndCategoryByIdAndDeletedFalse(3L)).thenReturn(Optional.of(post));
        when(likes.existsByPostIdAndUserId(3L, 1L)).thenReturn(true);
        when(files.findByPostIdOrderByCreatedAtAsc(3L)).thenReturn(List.of(PostFile.create(post, "a.png", "stored.png", "path", "image/png", 1)));
        assertThat(service.getPost(3L).likedByMe()).isFalse();
        var result = service.getPost(3L, 1L);
        assertThat(result.likedByMe()).isTrue();
        assertThat(result.files().get(0).fileUrl()).isEqualTo("/files/stored.png");
        assertThat(result.userName()).isEqualTo("nick");
    }

    @Test void missingDetailIsNotFound() {
        assertThatThrownBy(() -> service.getPost(3L)).extracting("errorCode").isEqualTo(ErrorCode.POST_NOT_FOUND);
    }

    @Test void updateChangesEditableFieldsAndPreservesOthers() {
        when(posts.findById(3L)).thenReturn(Optional.of(post));
        when(categories.findById(2L)).thenReturn(Optional.of(category));
        LocalDateTime created = post.getCreatedAt();
        ReflectionTestUtils.setField(post, "updatedAt", created.minusDays(1));
        var result = service.updatePost(3L, 1L, new PostUpdateRequest(2L, "new", "body"));
        assertThat(result.title()).isEqualTo("new");
        assertThat(result.content()).isEqualTo("body");
        assertThat(result.updatedAt()).isAfter(created.minusDays(1));
        assertThat(post.getCreatedAt()).isEqualTo(created);
        assertThat(post.getUser()).isSameAs(user);
        assertThat(post.isNewPost()).isTrue();
        assertThat(post.isDeleted()).isFalse();
        verify(storage).store(post, List.of());
    }

    @Test void updateRejectsMissingPostWrongAuthorAndMissingCategory() {
        var request = new PostUpdateRequest(2L, "new", "body");
        assertThatThrownBy(() -> service.updatePost(3L, 1L, request)).extracting("errorCode").isEqualTo(ErrorCode.POST_NOT_FOUND);
        when(posts.findById(3L)).thenReturn(Optional.of(post));
        assertThatThrownBy(() -> service.updatePost(3L, 9L, request)).extracting("errorCode").isEqualTo(ErrorCode.POST_AUTHOR_MISMATCH);
        assertThatThrownBy(() -> service.updatePost(3L, 1L, request)).extracting("errorCode").isEqualTo(ErrorCode.CATEGORY_NOT_FOUND);
        assertThat(post.getTitle()).isEqualTo("title");
        verifyNoInteractions(storage);
    }

    @Test void storageFailurePropagates() {
        when(posts.findById(3L)).thenReturn(Optional.of(post));
        when(categories.findById(2L)).thenReturn(Optional.of(category));
        when(storage.store(post, List.of())).thenThrow(new CustomException(ErrorCode.FILE_STORAGE_FAILED));
        assertThatThrownBy(() -> service.updatePost(3L, 1L, new PostUpdateRequest(2L, "new", "body")))
                .extracting("errorCode").isEqualTo(ErrorCode.FILE_STORAGE_FAILED);
        verifyNoInteractions(files);
    }

    @Test void deleteChecksAuthorAndSoftDeletes() {
        assertThatThrownBy(() -> service.deletePost(3L, 1L)).extracting("errorCode").isEqualTo(ErrorCode.POST_NOT_FOUND);
        when(posts.findByIdAndDeletedFalse(3L)).thenReturn(Optional.of(post));
        assertThatThrownBy(() -> service.deletePost(3L, 9L)).extracting("errorCode").isEqualTo(ErrorCode.POST_AUTHOR_MISMATCH);
        ReflectionTestUtils.setField(post, "updatedAt", LocalDateTime.now().minusDays(1));
        var before = post.getUpdatedAt();
        service.deletePost(3L, 1L);
        assertThat(post.isDeleted()).isTrue();
        assertThat(post.getUpdatedAt()).isAfter(before);
        verify(posts, never()).delete(any());
    }
}