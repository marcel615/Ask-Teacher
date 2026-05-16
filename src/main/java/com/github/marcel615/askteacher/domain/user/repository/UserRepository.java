package com.github.marcel615.askteacher.domain.user.repository;

import com.github.marcel615.askteacher.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);

}
