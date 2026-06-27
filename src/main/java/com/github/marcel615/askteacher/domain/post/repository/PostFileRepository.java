package com.github.marcel615.askteacher.domain.post.repository;

import com.github.marcel615.askteacher.domain.post.entity.PostFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostFileRepository extends JpaRepository<PostFile, Long> {

    List<PostFile> findByPostIdOrderByCreatedAtAsc(Long postId);
}
