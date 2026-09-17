package com.github.marcel615.askteacher.domain.comment.repository;

import com.github.marcel615.askteacher.domain.comment.dto.CommentResponse;
import com.github.marcel615.askteacher.domain.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Optional<Comment> findByIdAndDeletedFalse(Long id);

    @Query(value = """
            select new com.github.marcel615.askteacher.domain.comment.dto.CommentResponse(
                c.id, c.post.id, c.user.id, c.user.nickname, c.content, count(cl),
                case when count(case when :userId is not null and cl.user.id = :userId then 1 end) > 0 then true else false end,
                c.createdAt, c.updatedAt
            )
            from Comment c
            left join CommentLike cl on cl.comment = c
            where c.post.id = :postId and c.deleted = false
            group by c.id, c.post.id, c.user.id, c.user.nickname, c.content, c.createdAt, c.updatedAt
            order by c.createdAt desc, c.id desc
            """, countQuery = """
            select count(c) from Comment c where c.post.id = :postId and c.deleted = false
            """)
    Page<CommentResponse> findLatestPage(
            @Param("postId") Long postId,
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Query(value = """
            select new com.github.marcel615.askteacher.domain.comment.dto.CommentResponse(
                c.id, c.post.id, c.user.id, c.user.nickname, c.content, count(cl),
                case when count(case when :userId is not null and cl.user.id = :userId then 1 end) > 0 then true else false end,
                c.createdAt, c.updatedAt
            )
            from Comment c
            left join CommentLike cl on cl.comment = c
            where c.post.id = :postId and c.deleted = false
            group by c.id, c.post.id, c.user.id, c.user.nickname, c.content, c.createdAt, c.updatedAt
            order by count(cl) desc, c.createdAt desc, c.id desc
            """, countQuery = """
            select count(c) from Comment c where c.post.id = :postId and c.deleted = false
            """)
    Page<CommentResponse> findLikeCountPage(
            @Param("postId") Long postId,
            @Param("userId") Long userId,
            Pageable pageable
    );
}
