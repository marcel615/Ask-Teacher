package com.github.marcel615.askteacher.domain.post.repository;

import com.github.marcel615.askteacher.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

}
