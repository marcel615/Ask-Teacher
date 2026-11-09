package com.github.marcel615.askteacher.support;

import com.github.marcel615.askteacher.domain.auth.session.RedisSessionStore;
import com.github.marcel615.askteacher.global.config.SecurityConfig;
import com.github.marcel615.askteacher.global.security.AskTeacherUserDetails;
import com.github.marcel615.askteacher.global.security.AskTeacherUserDetailsService;
import com.github.marcel615.askteacher.global.security.jwt.JwtAuthenticationFilter;
import com.github.marcel615.askteacher.global.security.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;


import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

@ActiveProfiles("test")
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtTokenProvider.class})
public abstract class WebTestSupport {
    @Autowired
    protected MockMvc mvc;
    @MockitoBean
    protected RedisSessionStore sessions;
    @MockitoBean
    protected AskTeacherUserDetailsService details;

    protected RequestPostProcessor authenticated() {
        AskTeacherUserDetails principal = new AskTeacherUserDetails(7L, "", "", "USER");
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );
        return authentication(authentication);
    }
}
