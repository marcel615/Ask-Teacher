package com.github.marcel615.askteacher.domain.postlike.repository;

import com.github.marcel615.askteacher.domain.post.entity.Post;
import com.github.marcel615.askteacher.domain.postlike.entity.PostLike;
import com.github.marcel615.askteacher.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    boolean existsByPostAndUser(Post post, User user);

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    Optional<PostLike> findByPostAndUser(Post post, User user);
}
