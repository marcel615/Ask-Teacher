package com.github.marcel615.askteacher.domain.post.dto;

import com.github.marcel615.askteacher.domain.post.entity.PostFile;

public record PostFileResponse(
        String storedFileName,
        String contentType,
        String fileUrl
) {

    public static PostFileResponse from(PostFile postFile) {
        return new PostFileResponse(
                postFile.getStoredFileName(),
                postFile.getContentType(),
                "/files/" + postFile.getStoredFileName()
        );
    }
}
