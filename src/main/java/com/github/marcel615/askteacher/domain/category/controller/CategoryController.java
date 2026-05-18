package com.github.marcel615.askteacher.domain.category.controller;

import com.github.marcel615.askteacher.domain.category.dto.CategoryResponse;
import com.github.marcel615.askteacher.domain.category.service.CategoryService;
import com.github.marcel615.askteacher.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<CategoryResponse>> getCategories(){
        List<CategoryResponse> categoryResponses = categoryService.getCategories();

        return ApiResponse.success(200, "카테고리 목록이 반환에 성공하였습니다.", categoryResponses);
    }
}
