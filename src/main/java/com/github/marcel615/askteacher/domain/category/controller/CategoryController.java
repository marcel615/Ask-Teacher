package com.github.marcel615.askteacher.domain.category.controller;

import com.github.marcel615.askteacher.domain.category.dto.CategoryResponse;
import com.github.marcel615.askteacher.domain.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping()
    public ResponseEntity<List<CategoryResponse>> getCategories(){
        List<CategoryResponse> categoryResponses = categoryService.getCategories();

        return ResponseEntity.ok(categoryResponses);
    }
}
