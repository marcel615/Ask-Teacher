package com.github.marcel615.askteacher.domain.category.dto;

import com.github.marcel615.askteacher.domain.category.entity.Category;

public record CategoryResponse(
        Long id,
        String name
) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName()
        );
    }
}
