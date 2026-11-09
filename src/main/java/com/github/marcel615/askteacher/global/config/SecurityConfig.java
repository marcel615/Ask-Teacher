package com.github.marcel615.askteacher.global.config;

import com.github.marcel615.askteacher.global.security.AskTeacherUserDetailsService;
import com.github.marcel615.askteacher.global.security.SignedCsrfTokenRepository;
import com.github.marcel615.askteacher.global.security.jwt.JwtAuthenticationFilter;
import com.github.marcel615.askteacher.global.exception.ErrorCode;
import com.github.marcel615.askteacher.global.response.ErrorResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final boolean secure;
    private final String sameSite;
    private final String origins;
    private final String csrfSecret;
    public SecurityConfig(JwtAuthenticationFilter filter, @Value("${app.auth.cookie-secure:true}") boolean secure,
                          @Value("${app.auth.cookie-same-site:Strict}") String sameSite,
                          @Value("${app.auth.allowed-origins:http://localhost:5173}") String origins,
                          @Value("${app.auth.csrf-secret}") String csrfSecret,
                          Environment environment) {
        var profiles = List.of(environment.getActiveProfiles());
        if (!secure && (profiles.contains("prod") || (!profiles.contains("dev") && !profiles.contains("test")) || "None".equalsIgnoreCase(sameSite)))
            throw new IllegalStateException("Secure cookies are required in production or with SameSite=None");
        if (!List.of("Strict", "Lax", "None").contains(sameSite)) throw new IllegalStateException("Invalid SameSite setting");
        this.jwtAuthenticationFilter = filter; this.secure = secure; this.sameSite = sameSite; this.origins = origins; this.csrfSecret = csrfSecret;
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper mapper) throws Exception {
        SignedCsrfTokenRepository csrfRepo = new SignedCsrfTokenRepository(csrfSecret, secure, sameSite);
        CsrfTokenRequestAttributeHandler csrfHandler = new CsrfTokenRequestAttributeHandler();
        var paths = PathPatternRequestMatcher.withDefaults();
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.csrfTokenRepository(csrfRepo).csrfTokenRequestHandler(csrfHandler)
                .requireCsrfProtectionMatcher(new OrRequestMatcher(paths.matcher(HttpMethod.POST, "/api/auth/login"),
                        paths.matcher(HttpMethod.POST, "/api/auth/refresh"), paths.matcher(HttpMethod.POST, "/api/auth/logout"))))
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            .formLogin(form -> form.disable()).httpBasic(basic -> basic.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, error) -> writeError(response, mapper, ErrorCode.UNAUTHORIZED))
                .accessDeniedHandler((request, response, error) -> {
                    response.setStatus(403); response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                    mapper.writeValue(response.getWriter(), ErrorResponse.of(403, "접근이 거부되었습니다."));
                }))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/login", "/api/auth/signup", "/api/auth/refresh", "/api/auth/logout", "/api/auth/csrf").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/categories", "/api/posts", "/api/posts/{postId}", "/api/posts/{postId}/comments", "/files/**").permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
    private static void writeError(HttpServletResponse response, ObjectMapper mapper, ErrorCode code) throws IOException {
        response.setStatus(code.getStatus()); response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        mapper.writeValue(response.getWriter(), ErrorResponse.of(code.getStatus(), code.getMessage()));
    }
    @Bean public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(origins.split(",")).map(String::trim).toList());
        config.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Content-Type", "Authorization", "X-XSRF-TOKEN"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(); source.registerCorsConfiguration("/**", config); return source;
    }
    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean public AuthenticationManager authenticationManager(AskTeacherUserDetailsService details, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(details); provider.setPasswordEncoder(encoder); return new ProviderManager(provider);
    }
}
