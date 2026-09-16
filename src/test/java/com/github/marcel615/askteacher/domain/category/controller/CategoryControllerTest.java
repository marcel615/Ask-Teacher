package com.github.marcel615.askteacher.domain.category.controller;

import com.github.marcel615.askteacher.domain.category.dto.CategoryResponse;
import com.github.marcel615.askteacher.domain.category.service.CategoryService;
import com.github.marcel615.askteacher.support.WebTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest extends WebTestSupport {
    @MockitoBean CategoryService service;

    @Test void publicListReturnsArrayAndEmptyArray() throws Exception {
        when(service.getCategories()).thenReturn(List.of(new CategoryResponse(2L, "Java")), List.of());
        mvc.perform(get("/api/categories")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2)).andExpect(jsonPath("$[0].name").value("Java"));
        mvc.perform(get("/api/categories")).andExpect(status().isOk()).andExpect(content().json("[]"));
    }
}
