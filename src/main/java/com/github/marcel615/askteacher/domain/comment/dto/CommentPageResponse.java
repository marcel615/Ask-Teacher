package com.github.marcel615.askteacher.domain.comment.dto;

import com.github.marcel615.askteacher.domain.comment.type.CommentSort;
import org.springframework.data.domain.Page;

import java.util.List;

public record CommentPageResponse(
        List<CommentResponse> comments,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        String sort
) {
    public static CommentPageResponse from(Page<CommentResponse> comments, CommentSort sort) {
        return new CommentPageResponse(
                comments.getContent(),
                comments.getNumber(),
                comments.getSize(),
                comments.getTotalElements(),
                comments.getTotalPages(),
                comments.hasNext(),
                sort.getValue()
        );
    }
}
