package com.github.marcel615.askteacher.domain.category.entity;

import com.github.marcel615.askteacher.global.entity.BaseUpdatableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "categories")
public class Category extends BaseUpdatableEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    public static Category createCategory(String name) {
        Category category = new Category();

        category.name = name;

        return category;
    }

}
