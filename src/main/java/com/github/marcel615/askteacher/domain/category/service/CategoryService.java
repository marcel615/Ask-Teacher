package com.github.marcel615.askteacher.domain.category.service;

import com.github.marcel615.askteacher.domain.category.dto.CategoryResponse;
import com.github.marcel615.askteacher.domain.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    @Transactional
    public List<CategoryResponse> getCategories() {
        return categoryRepository.findAll().stream()
                .map(category -> CategoryResponse.from(category))
                .toList();
    }
}
