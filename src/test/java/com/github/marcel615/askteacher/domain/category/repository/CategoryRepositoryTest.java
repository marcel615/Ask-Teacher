package com.github.marcel615.askteacher.domain.category.repository;

import com.github.marcel615.askteacher.domain.category.entity.Category;
import com.github.marcel615.askteacher.support.RepositoryTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import static org.assertj.core.api.Assertions.*;

class CategoryRepositoryTest extends RepositoryTestSupport {
    @Autowired CategoryRepository repository;

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

    @Test void nameMustBeUnique() {
        category("Java");
        flushAndClear();
        assertThatThrownBy(() -> repository.saveAndFlush(Category.createCategory("Java")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
