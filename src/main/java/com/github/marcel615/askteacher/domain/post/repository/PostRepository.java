package com.github.marcel615.askteacher.domain.post.repository;

import com.github.marcel615.askteacher.domain.post.entity.Post;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @EntityGraph(attributePaths = "user")
    List<Post> findByDeletedFalseOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"user", "category"})
    Optional<Post> findWithUserAndCategoryByIdAndDeletedFalse(Long id);

    Optional<Post> findByIdAndDeletedFalse(Long id);

    @Modifying
    @Query("update Post p set p.likeCount = p.likeCount + 1 where p.id = :postId")
    int increaseLikeCount(@Param("postId") Long postId);

    @Modifying
    @Query("update Post p set p.likeCount = p.likeCount - 1 where p.id = :postId and p.likeCount > 0")
    int decreaseLikeCount(@Param("postId") Long postId);

}
