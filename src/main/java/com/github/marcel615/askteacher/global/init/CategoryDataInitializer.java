package com.github.marcel615.askteacher.global.init;

import com.github.marcel615.askteacher.domain.category.entity.Category;
import com.github.marcel615.askteacher.domain.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryDataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args){
        createCategory("Java");
        createCategory("Spring");
        createCategory("Database");
        createCategory("Git");
        createCategory("기타");

    }

    private void createCategory(String categoryName){
        if (!categoryRepository.existsByName(categoryName)){
            categoryRepository.save(Category.createCategory(categoryName));
        }
    }

}
