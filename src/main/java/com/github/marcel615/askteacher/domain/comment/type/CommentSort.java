package com.github.marcel615.askteacher.domain.comment.type;

import com.github.marcel615.askteacher.global.exception.CustomException;
import com.github.marcel615.askteacher.global.exception.ErrorCode;

import java.util.Arrays;

public enum CommentSort {
    LATEST("latest"),
    LIKE_COUNT("likeCount");

    private final String value;

    CommentSort(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static CommentSort from(String value) {
        return Arrays.stream(values())
                .filter(sort -> sort.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE));
    }
}
