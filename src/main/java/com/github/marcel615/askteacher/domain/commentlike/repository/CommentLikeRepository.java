package com.github.marcel615.askteacher.domain.commentlike.repository;

import com.github.marcel615.askteacher.domain.commentlike.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    boolean existsByCommentIdAndUserId(Long commentId, Long userId);

    Optional<CommentLike> findByCommentIdAndUserId(Long commentId, Long userId);

    long countByCommentId(Long commentId);

    @Modifying(flushAutomatically = true)
    @Query("delete from CommentLike cl where cl.comment.id = :commentId")
    int deleteByCommentId(@Param("commentId") Long commentId);
}
