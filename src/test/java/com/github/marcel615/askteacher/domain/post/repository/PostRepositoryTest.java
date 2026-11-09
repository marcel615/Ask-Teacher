package com.github.marcel615.askteacher.domain.post.repository;

import com.github.marcel615.askteacher.domain.post.entity.Post;
import com.github.marcel615.askteacher.support.RepositoryTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.*;

class PostRepositoryTest extends RepositoryTestSupport {
    @Nested
    @DisplayName("게시글 저장과 조회")
    class Scenarios {
        @Autowired PostRepository repository;

        @DisplayName("검색은 제목 내용 카테고리 페이지 정렬과 삭제 필터를 조합한다")
        @Test void searchCombinesTitleContentCategoryPagingSortAndDeletedFilter() {
            var user = user("alice");
            var java = category("Java");
            var spring = category("Spring");
            var old = post(user, java, "needle title", "body");
            ReflectionTestUtils.setField(old, "createdAt", LocalDateTime.now().minusDays(2));
            var recent = post(user, java, "title", "needle body");
            ReflectionTestUtils.setField(recent, "createdAt", LocalDateTime.now().minusDays(1));
            var other = post(user, spring, "needle other", "body");
            var deleted = post(user, java, "needle deleted", "body");
            deleted.delete();
            flushAndClear();
            var first = repository.searchPosts("needle", java.getId(), PageRequest.of(0, 1, Sort.by("createdAt").descending()));
            assertThat(first.getTotalElements()).isEqualTo(2);
            assertThat(first.getTotalPages()).isEqualTo(2);
            assertThat(first.getContent()).extracting(Post::getId).containsExactly(recent.getId());
            assertThat(first.getContent().get(0).getUser().getNickname()).isEqualTo("alice");
            assertThat(repository.searchPosts("needle", java.getId(), PageRequest.of(1, 1, Sort.by("createdAt").descending())).getContent())
                    .extracting(Post::getId).containsExactly(old.getId());
            assertThat(repository.searchPosts(null, null, Pageable.unpaged()).getContent()).extracting(Post::getId)
                    .containsExactlyInAnyOrder(old.getId(), recent.getId(), other.getId());
            assertThat(repository.searchPosts("absent", null, Pageable.unpaged())).isEmpty();
            assertThat(repository.searchPosts(null, spring.getId(), Pageable.unpaged()).getContent()).extracting(Post::getId).containsExactly(other.getId());
            assertThat(repository.findByDeletedFalseOrderByCreatedAtDesc()).extracting(Post::getId)
                    .containsExactly(other.getId(), recent.getId(), old.getId());
            assertThat(repository.findByIdAndDeletedFalse(deleted.getId())).isEmpty();
            assertThat(repository.findByIdAndDeletedFalse(old.getId())).isPresent();
            assertThat(repository.findByIdAndDeletedFalse(-1L)).isEmpty();
            assertThat(repository.findWithUserAndCategoryByIdAndDeletedFalse(deleted.getId())).isEmpty();
            assertThat(repository.findWithUserAndCategoryByIdAndDeletedFalse(old.getId()).orElseThrow().getCategory().getName()).isEqualTo("Java");
        }

        @DisplayName("좋아요 일괄 변경은 재조회에 반영되고 0 미만이 되지 않는다")
        @Test void bulkLikeUpdatesAreVisibleAfterReloadAndNeverGoBelowZero() {
            var post = post(user("alice"), category("Java"), "title", "body");
            Long id = post.getId();
            flushAndClear();
            assertThat(repository.decreaseLikeCount(id)).isZero();
            assertThat(repository.increaseLikeCount(id)).isEqualTo(1);
            flushAndClear();
            assertThat(repository.findById(id).orElseThrow().getLikeCount()).isEqualTo(1);
            assertThat(repository.decreaseLikeCount(id)).isEqualTo(1);
            flushAndClear();
            assertThat(repository.findById(id).orElseThrow().getLikeCount()).isZero();
            assertThat(repository.increaseLikeCount(-1L)).isZero();
        }

        @DisplayName("변경 감지로 게시글 수정과 논리 삭제를 저장한다")
        @Test void dirtyCheckingPersistsUpdateAndSoftDelete() {
            var post = post(user("alice"), category("Java"), "title", "body");
            var other = category("Spring");
            post.update(other, "changed", "changed body");
            post.delete();
            flushAndClear();
            var saved = repository.findById(post.getId()).orElseThrow();
            assertThat(saved.getTitle()).isEqualTo("changed");
            assertThat(saved.getContent()).isEqualTo("changed body");
            assertThat(saved.getCategory().getId()).isEqualTo(other.getId());
            assertThat(saved.isDeleted()).isTrue();
            assertThat(saved.isNewPost()).isTrue();
        }
    }
}
