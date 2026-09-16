package com.github.marcel615.askteacher.support;

import com.github.marcel615.askteacher.global.config.SecurityConfig;
import com.github.marcel615.askteacher.global.security.jwt.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

@ActiveProfiles("test")
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtTokenProvider.class})
public abstract class WebTestSupport {
    @Autowired protected MockMvc mvc;

    protected RequestPostProcessor authenticated() {
        return authentication(new UsernamePasswordAuthenticationToken(7L, null, List.of(new SimpleGrantedAuthority("USER"))));
    }
}
