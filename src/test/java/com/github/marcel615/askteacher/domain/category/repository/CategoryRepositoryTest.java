package com.github.marcel615.askteacher.domain.category.repository;

import com.github.marcel615.askteacher.domain.category.entity.Category;
import com.github.marcel615.askteacher.support.RepositoryTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import static org.assertj.core.api.Assertions.*;

class CategoryRepositoryTest extends RepositoryTestSupport {
    @org.junit.jupiter.api.Nested
    @org.junit.jupiter.api.DisplayName("카테고리 저장과 조회")
    class Scenarios {
        @Autowired CategoryRepository repository;

        @org.junit.jupiter.api.DisplayName("카테고리를 저장하고 조회한다")
        @Test void savesAndFindsCategory() {
            assertThat(repository.findAll()).isEmpty();
            Long id = category("Java").getId();
            flushAndClear();
            var found = repository.findByName("Java").orElseThrow();
            assertThat(found.getId()).isEqualTo(id);
            assertThat(found.getCreatedAt()).isNotNull();
            assertThat(found.getUpdatedAt()).isNotNull();
            assertThat(repository.existsByName("Java")).isTrue();
            assertThat(repository.existsByName("missing")).isFalse();
            assertThat(repository.findByName("missing")).isEmpty();
            assertThat(repository.findAll()).extracting(Category::getName).containsExactly("Java");
        }

        @org.junit.jupiter.api.DisplayName("카테고리 이름의 중복을 거부한다")
        @Test void nameMustBeUnique() {
            category("Java");
            flushAndClear();
            assertThatThrownBy(() -> repository.saveAndFlush(Category.createCategory("Java")))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }
    }
}
