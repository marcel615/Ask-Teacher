package com.github.marcel615.askteacher.domain.post.storage;

import com.github.marcel615.askteacher.domain.post.entity.Post;
import com.github.marcel615.askteacher.domain.post.entity.PostFile;
import com.github.marcel615.askteacher.global.exception.CustomException;
import com.github.marcel615.askteacher.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class PostFileStorage {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "application/pdf"
    );

    private final Path storagePath;
    private final long maxFileSize;

    public PostFileStorage(
            @Value("${app.file-storage.post-upload-dir:uploads/posts}") String postUploadDir,
            @Value("${app.file-storage.max-file-size:10485760}") long maxFileSize
    ) {
        this.storagePath = Path.of(postUploadDir).toAbsolutePath().normalize();
        this.maxFileSize = maxFileSize;
    }

    public List<PostFile> store(Post post, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return List.of();
        }

        return files.stream()
                .map(file -> store(post, file))
                .toList();
    }

    private PostFile store(Post post, MultipartFile file) {
        validate(file);

        String originalFileName = cleanOriginalFileName(file.getOriginalFilename());
        String storedFileName = UUID.randomUUID() + extractExtension(originalFileName);
        Path targetPath = storagePath.resolve(storedFileName).normalize();

        try {
            Files.createDirectories(storagePath);
            file.transferTo(targetPath);
        } catch (IOException | IllegalStateException exception) {
            throw new CustomException(ErrorCode.FILE_STORAGE_FAILED);
        }

        return PostFile.create(
                post,
                originalFileName,
                storedFileName,
                targetPath.toString(),
                file.getContentType(),
                file.getSize()
        );
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.EMPTY_FILE);
        }

        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new CustomException(ErrorCode.UNSUPPORTED_FILE_TYPE);
        }

        if (file.getSize() > maxFileSize) {
            throw new CustomException(ErrorCode.FILE_SIZE_EXCEEDED);
        }
    }

    private String extractExtension(String originalFileName) {
        if (!StringUtils.hasText(originalFileName)) {
            return "";
        }

        int extensionIndex = originalFileName.lastIndexOf('.');
        if (extensionIndex < 0) {
            return "";
        }

        return originalFileName.substring(extensionIndex);
    }

    private String cleanOriginalFileName(String originalFileName) {
        if (!StringUtils.hasText(originalFileName)) {
            return "file";
        }

        return StringUtils.cleanPath(originalFileName);
    }
}
