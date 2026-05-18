package com.github.marcel615.askteacher.domain.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostCreateRequest {

    @NotBlank(message = "작성자를 입력해주세요.")
    private Long userId;

    @NotBlank(message = "작성 글의 카테고리를 작성해주세요.")
    private String categoryName;

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 100, message = "질문 내용은 100자 이하로 입력해야 합니다.")
    private String title;

    @NotBlank(message = "질문 내용은 필수입니다.")
    @Size(max = 5000, message = "질문 내용은 5000자 이하로 입력해야 합니다.")
    private String content;

}
