package com.github.marcel615.askteacher.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class FileStorageConfig implements WebMvcConfigurer {

    private final String postUploadDir;

    public FileStorageConfig(@Value("${app.file-storage.post-upload-dir:uploads/posts}") String postUploadDir) {
        this.postUploadDir = postUploadDir;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadLocation = Path.of(postUploadDir)
                .toAbsolutePath()
                .normalize()
                .toUri()
                .toString();

        registry.addResourceHandler("/files/**")
                .addResourceLocations(uploadLocation);
    }
}
