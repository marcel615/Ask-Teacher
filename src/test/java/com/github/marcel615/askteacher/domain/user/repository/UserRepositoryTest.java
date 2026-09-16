package com.github.marcel615.askteacher.domain.user.repository;

import com.github.marcel615.askteacher.domain.user.entity.User;
import com.github.marcel615.askteacher.domain.user.type.UserRole;
import com.github.marcel615.askteacher.support.RepositoryTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import static org.assertj.core.api.Assertions.*;

class UserRepositoryTest extends RepositoryTestSupport {
    @Autowired UserRepository repository;

    @Test void persistsAndFindsUserByEmailAndNickname() {
        Long id = user("alice").getId();
        flushAndClear();
        User found = repository.findByEmail("alice@example.com").orElseThrow();
        assertThat(found.getId()).isEqualTo(id);
        assertThat(found.getPassword()).isEqualTo("encoded");
        assertThat(found.getRole()).isEqualTo(UserRole.USER);
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
        assertThat(repository.existsByEmail(found.getEmail())).isTrue();
        assertThat(repository.existsByNickname("alice")).isTrue();
        assertThat(repository.findByEmail("missing@example.com")).isEmpty();
        assertThat(repository.existsByNickname("missing")).isFalse();
    }

    @Test void emailMustBeUnique() {
        user("alice");
        flushAndClear();
        assertThatThrownBy(() -> repository.saveAndFlush(User.createUser("alice@example.com", "encoded", "bob")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test void nicknameMustBeUnique() {
        user("alice");
        flushAndClear();
        assertThatThrownBy(() -> repository.saveAndFlush(User.createUser("bob@example.com", "encoded", "alice")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
