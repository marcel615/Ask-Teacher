package com.github.marcel615.askteacher.domain.post.repository;

import com.github.marcel615.askteacher.domain.post.entity.PostFile;
import com.github.marcel615.askteacher.support.RepositoryTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.*;

class PostFileRepositoryTest extends RepositoryTestSupport {
    @org.junit.jupiter.api.Nested
    @org.junit.jupiter.api.DisplayName("게시글 파일 저장과 조회")
    class Scenarios {
        @Autowired PostFileRepository repository;

        @org.junit.jupiter.api.DisplayName("게시글별 파일을 순서대로 조회하고 메타데이터를 저장한다")
        @Test void filesAreScopedToPostOrderedAndMetadataIsPersisted() {
            var user = user("alice");
            var category = category("Java");
            var post = post(user, category, "title", "body");
            var other = post(user, category, "other", "body");
            var recent = PostFile.create(post, "recent.pdf", "recent.pdf", "/test/recent.pdf", "application/pdf", 2);
            var old = PostFile.create(post, "old.png", "old.png", "/test/old.png", "image/png", 1);
            repository.saveAndFlush(recent);
            repository.saveAndFlush(old);
            ReflectionTestUtils.setField(old, "createdAt", recent.getCreatedAt().minusDays(1));
            repository.save(PostFile.create(other, "other.png", "other.png", "/test/other.png", "image/png", 1));
            flushAndClear();
            var found = repository.findByPostIdOrderByCreatedAtAsc(post.getId());
            assertThat(found).extracting(PostFile::getStoredFileName).containsExactly("old.png", "recent.pdf");
            assertThat(found.get(0).getOriginalFileName()).isEqualTo("old.png");
            assertThat(found.get(0).getFilePath()).isEqualTo("/test/old.png");
            assertThat(found.get(0).getFileSize()).isEqualTo(1);
            assertThat(found.get(0).getContentType()).isEqualTo("image/png");
            assertThat(repository.findByPostIdOrderByCreatedAtAsc(-1L)).isEmpty();
        }

        @org.junit.jupiter.api.DisplayName("저장 파일 이름의 중복을 거부한다")
        @Test void storedFileNameMustBeUnique() {
            var post = post(user("alice"), category("Java"), "title", "body");
            repository.saveAndFlush(PostFile.create(post, "a.png", "same.png", "path", "image/png", 1));
            flushAndClear();
            assertThatThrownBy(() -> repository.saveAndFlush(PostFile.create(post, "b.png", "same.png", "path", "image/png", 1)))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }
    }
}
