package com.github.marcel615.askteacher.domain.postlike.repository;

import com.github.marcel615.askteacher.domain.postlike.entity.PostLike;
import com.github.marcel615.askteacher.support.RepositoryTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import static org.assertj.core.api.Assertions.*;

class PostLikeRepositoryTest extends RepositoryTestSupport {
    @Nested
    @DisplayName("게시글 좋아요 저장과 조회")
    class Scenarios {
        @Autowired PostLikeRepository repository;

        @DisplayName("일치하는 사용자와 게시글의 좋아요만 조회하고 관계를 삭제한다")
        @Test void findsOnlyMatchingUserAndPostAndDeletesRelation() {
            var alice = user("alice");
            var bob = user("bob");
            var category = category("Java");
            var post = post(alice, category, "title", "body");
            var other = post(alice, category, "other", "body");
            var saved = repository.saveAndFlush(PostLike.create(post, alice));
            flushAndClear();
            assertThat(repository.existsByPostAndUser(post, alice)).isTrue();
            assertThat(repository.existsByPostIdAndUserId(post.getId(), alice.getId())).isTrue();
            assertThat(repository.existsByPostAndUser(post, bob)).isFalse();
            assertThat(repository.findByPostAndUser(other, alice)).isEmpty();
            var found = repository.findByPostAndUser(post, alice).orElseThrow();
            assertThat(found.getId()).isEqualTo(saved.getId());
            assertThat(found.getCreatedAt()).isNotNull();
            repository.delete(found);
            flushAndClear();
            assertThat(repository.findByPostAndUser(post, alice)).isEmpty();
        }

        @DisplayName("같은 사용자는 같은 게시글에 중복 좋아요를 할 수 없다")
        @Test void sameUserCannotLikeSamePostTwice() {
            var user = user("alice");
            var post = post(user, category("Java"), "title", "body");
            repository.saveAndFlush(PostLike.create(post, user));
            flushAndClear();
            assertThatThrownBy(() -> repository.saveAndFlush(PostLike.create(post, user)))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }
    }
}
