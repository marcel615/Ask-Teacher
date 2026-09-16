package com.github.marcel615.askteacher.domain.category.service;

import com.github.marcel615.askteacher.domain.category.entity.Category;
import com.github.marcel615.askteacher.domain.category.repository.CategoryRepository;
import com.github.marcel615.askteacher.domain.category.dto.CategoryResponse;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CategoryServiceTest {
    private final CategoryRepository repository = mock(CategoryRepository.class);
    private final CategoryService service = new CategoryService(repository);

    @Test void mapsCategoriesAndHandlesEmptyList() {
        Category category = Category.createCategory("Java");
        when(repository.findAll()).thenReturn(List.of(category), List.of());
        assertThat(service.getCategories()).containsExactly(CategoryResponse.from(category));
        assertThat(service.getCategories()).isEmpty();
    }
}
