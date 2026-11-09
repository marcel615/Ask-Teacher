package com.github.marcel615.askteacher.domain.post.storage;

import com.github.marcel615.askteacher.domain.post.entity.Post;
import com.github.marcel615.askteacher.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.*;
import java.io.IOException;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PostFileStorageTest {
    @Nested
    @DisplayName("게시글 파일 저장")
    class Scenarios {
        @TempDir Path directory;
        private final Post post = Post.createPost(null, null, "title", "body");

        @DisplayName("허용된 MIME과 크기 경계의 파일 바이트와 메타데이터를 저장한다")
        @ParameterizedTest
        @ValueSource(strings = {"image/jpeg", "image/png", "image/webp", "application/pdf"})
        void allowedMimeAndExactSizeBoundarySaveBytesAndMetadata(String mime) throws Exception {
            var storage = new PostFileStorage(directory.toString(), 4);
            byte[] bytes = {1, 2, 3, 4};
            var file = storage.store(post, List.of(new MockMultipartFile("files", "test.file", mime, bytes))).get(0);
            assertThat(file.getPost()).isSameAs(post);
            assertThat(file.getOriginalFileName()).isEqualTo("test.file");
            assertThat(file.getStoredFileName()).endsWith(".file").isNotEqualTo("test.file");
            assertThat(file.getFileSize()).isEqualTo(4);
            assertThat(file.getContentType()).isEqualTo(mime);
            assertThat(Path.of(file.getFilePath()).getParent()).isEqualTo(directory.toAbsolutePath());
            assertThat(Files.readAllBytes(Path.of(file.getFilePath()))).containsExactly(bytes);
        }

        @DisplayName("파일이 없으면 아무것도 생성하지 않는다")
        @Test void noFilesDoesNotCreateAnything() throws Exception {
            var storage = new PostFileStorage(directory.resolve("unused").toString(), 4);
            assertThat(storage.store(post, null)).isEmpty();
            assertThat(storage.store(post, List.of())).isEmpty();
            assertThat(directory.resolve("unused")).doesNotExist();
        }

        @DisplayName("빈 파일과 미지원 파일 크기 초과 파일을 쓰기 전에 거부한다")
        @Test void rejectsEmptyUnsupportedAndOversizedFilesBeforeWriting() throws Exception {
            var storage = new PostFileStorage(directory.toString(), 4);
            assertThatThrownBy(() -> storage.store(post, Collections.singletonList(null))).extracting("errorCode").isEqualTo(ErrorCode.EMPTY_FILE);
            assertThatThrownBy(() -> storage.store(post, List.of(new MockMultipartFile("files", "a.png", "image/png", new byte[0]))))
                    .extracting("errorCode").isEqualTo(ErrorCode.EMPTY_FILE);
            assertThatThrownBy(() -> storage.store(post, List.of(new MockMultipartFile("files", "a.txt", "text/plain", new byte[1]))))
                    .extracting("errorCode").isEqualTo(ErrorCode.UNSUPPORTED_FILE_TYPE);
            assertThatThrownBy(() -> storage.store(post, List.of(new MockMultipartFile("files", "a.png", "image/png", new byte[5]))))
                    .extracting("errorCode").isEqualTo(ErrorCode.FILE_SIZE_EXCEEDED);
            try (var paths = Files.list(directory)) { assertThat(paths).isEmpty(); }
        }

        @DisplayName("입출력과 상태 오류를 파일 저장 오류로 변환한다")
        @Test void ioAndIllegalStateFailuresBecomeStorageError() throws Exception {
            MultipartFile file = mock(MultipartFile.class);
            when(file.getOriginalFilename()).thenReturn("a.png");
            when(file.getContentType()).thenReturn("image/png");
            when(file.getSize()).thenReturn(1L);
            doThrow(new IOException("test failure")).doThrow(new IllegalStateException("test failure")).when(file).transferTo(any(Path.class));
            var storage = new PostFileStorage(directory.toString(), 4);
            for (int attempt = 0; attempt < 2; attempt++) {
                assertThatThrownBy(() -> storage.store(post, List.of(file))).extracting("errorCode").isEqualTo(ErrorCode.FILE_STORAGE_FAILED);
            }
        }
    }
}
