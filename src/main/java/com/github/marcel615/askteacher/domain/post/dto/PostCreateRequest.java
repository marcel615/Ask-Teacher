package com.github.marcel615.askteacher.domain.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PostCreateRequest(
        @NotNull(message = "작성 글의 카테고리를 선택해주세요.")
        Long categoryId,

        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 100, message = "제목은 100자 이하로 입력해야 합니다.")
        String title,

        @NotBlank(message = "질문 내용은 필수입니다.")
        @Size(max = 5000, message = "질문 내용은 5000자 이하로 입력해야 합니다.")
        String content
) {
}
