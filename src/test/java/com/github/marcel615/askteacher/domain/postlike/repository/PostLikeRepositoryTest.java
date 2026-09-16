package com.github.marcel615.askteacher.domain.postlike.repository;

import com.github.marcel615.askteacher.domain.postlike.entity.PostLike;
import com.github.marcel615.askteacher.support.RepositoryTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import static org.assertj.core.api.Assertions.*;

class PostLikeRepositoryTest extends RepositoryTestSupport {
    @Autowired PostLikeRepository repository;

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

    @Test void sameUserCannotLikeSamePostTwice() {
        var user = user("alice");
        var post = post(user, category("Java"), "title", "body");
        repository.saveAndFlush(PostLike.create(post, user));
        flushAndClear();
        assertThatThrownBy(() -> repository.saveAndFlush(PostLike.create(post, user)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
