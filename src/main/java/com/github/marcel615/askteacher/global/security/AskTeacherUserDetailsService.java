package com.github.marcel615.askteacher.global.security;

import com.github.marcel615.askteacher.domain.user.repository.UserRepository;
import com.github.marcel615.askteacher.domain.user.entity.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AskTeacherUserDetailsService implements UserDetailsService {
    private final UserRepository users;
    public AskTeacherUserDetailsService(UserRepository users) {
        this.users = users;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) {
        User user = users.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new AskTeacherUserDetails(user.getId(), user.getEmail(), user.getPassword(), user.getRole().name());
    }
}
