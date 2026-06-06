package com.github.marcel615.askteacher.domain.post.repository;

import com.github.marcel615.askteacher.domain.post.entity.Post;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @EntityGraph(attributePaths = "user")
    List<Post> findByDeletedFalse();

    @EntityGraph(attributePaths = {"user", "category"})
    Optional<Post> findWithUserAndCategoryById(Long id);

    Optional<Post> findByIdAndDeletedFalse(Long id);

}
