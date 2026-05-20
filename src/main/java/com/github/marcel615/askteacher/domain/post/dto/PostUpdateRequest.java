package com.github.marcel615.askteacher.domain.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostUpdateRequest {

    @NotNull(message = "User id is required.")
    private Long userId;

    @NotNull(message = "Category id is required.")
    private Long categoryId;

    @NotBlank(message = "Title is required.")
    @Size(max = 100, message = "Title must be 100 characters or less.")
    private String title;

    @NotBlank(message = "Content is required.")
    @Size(max = 5000, message = "Content must be 5000 characters or less.")
    private String content;
}
