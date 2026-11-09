package com.github.marcel615.askteacher.domain.category.service;

import com.github.marcel615.askteacher.domain.category.entity.Category;
import com.github.marcel615.askteacher.domain.category.repository.CategoryRepository;
import com.github.marcel615.askteacher.domain.category.dto.CategoryResponse;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CategoryServiceTest {
    @org.junit.jupiter.api.Nested
    @org.junit.jupiter.api.DisplayName("카테고리 목록 조회")
    class Scenarios {
        private final CategoryRepository repository = mock(CategoryRepository.class);
        private final CategoryService service = new CategoryService(repository);

        @org.junit.jupiter.api.DisplayName("카테고리를 변환하고 빈 목록을 처리한다")
        @Test void mapsCategoriesAndHandlesEmptyList() {
            Category category = Category.createCategory("Java");
            when(repository.findAll()).thenReturn(List.of(category), List.of());
            assertThat(service.getCategories()).containsExactly(CategoryResponse.from(category));
            assertThat(service.getCategories()).isEmpty();
        }
    }
}
