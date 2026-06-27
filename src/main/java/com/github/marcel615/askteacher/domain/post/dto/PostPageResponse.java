package com.github.marcel615.askteacher.domain.post.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PostPageResponse(
        List<PostListResponse> content,
        int currentPage,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious,
        boolean isFirst,
        boolean isLast
) {

    public static PostPageResponse from(Page<PostListResponse> page) {
        return new PostPageResponse(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious(),
                page.isFirst(),
                page.isLast()
        );
    }
}
