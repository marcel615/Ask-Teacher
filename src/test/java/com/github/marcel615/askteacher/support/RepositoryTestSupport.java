package com.github.marcel615.askteacher.support;

import com.github.marcel615.askteacher.domain.category.entity.Category;
import com.github.marcel615.askteacher.domain.post.entity.Post;
import com.github.marcel615.askteacher.domain.user.entity.User;
import com.github.marcel615.askteacher.global.config.JpaAuditingConfig;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/** DB-backed JPA slice tests; H2 does not verify MySQL-specific behavior. */
@DataJpaTest
@ActiveProfiles("test")
@Import(JpaAuditingConfig.class)
public abstract class RepositoryTestSupport {
    @Autowired protected EntityManager em;

    protected User user(String name) {
        User user = User.createUser(name + "@example.com", "encoded", name);
        em.persist(user);
        return user;
    }

    protected Category category(String name) {
        Category category = Category.createCategory(name);
        em.persist(category);
        return category;
    }

    protected Post post(User user, Category category, String title, String content) {
        Post post = Post.createPost(user, category, title, content);
        em.persist(post);
        return post;
    }

    protected void flushAndClear() {
        em.flush();
        em.clear();
    }
}
