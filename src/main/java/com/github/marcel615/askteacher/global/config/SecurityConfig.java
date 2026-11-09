package com.github.marcel615.askteacher.global.config;

import com.github.marcel615.askteacher.global.exception.ErrorCode;
import com.github.marcel615.askteacher.global.response.ErrorResponse;
import com.github.marcel615.askteacher.global.security.AskTeacherUserDetailsService;
import com.github.marcel615.askteacher.global.security.SignedCsrfTokenRepository;
import com.github.marcel615.askteacher.global.security.jwt.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
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

    public SecurityConfig(
            JwtAuthenticationFilter filter,
            @Value("${app.auth.cookie-secure:true}") boolean secure,
            @Value("${app.auth.cookie-same-site:Strict}") String sameSite,
            @Value("${app.auth.allowed-origins:http://localhost:5173}") String origins,
            @Value("${app.auth.csrf-secret}") String csrfSecret,
            Environment environment
    ) {
        List<String> profiles = List.of(environment.getActiveProfiles());
        boolean productionProfile = profiles.contains("prod");
        boolean developmentOrTestProfile = profiles.contains("dev") || profiles.contains("test");
        boolean sameSiteNone = "None".equalsIgnoreCase(sameSite);
        if (!secure && (productionProfile || !developmentOrTestProfile || sameSiteNone)) {
            throw new IllegalStateException("Secure cookies are required in production or with SameSite=None");
        }
        if (!List.of("Strict", "Lax", "None").contains(sameSite)) {
            throw new IllegalStateException("Invalid SameSite setting");
        }
        this.jwtAuthenticationFilter = filter;
        this.secure = secure;
        this.sameSite = sameSite;
        this.origins = origins;
        this.csrfSecret = csrfSecret;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SignedCsrfTokenRepository csrfTokenRepository,
            CsrfTokenRequestAttributeHandler csrfRequestHandler,
            AuthenticationEntryPoint authenticationEntryPoint,
            AccessDeniedHandler accessDeniedHandler
    ) throws Exception {
        PathPatternRequestMatcher.Builder paths = PathPatternRequestMatcher.withDefaults();
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        http.csrf(csrf -> csrf
                .csrfTokenRepository(csrfTokenRepository)
                .csrfTokenRequestHandler(csrfRequestHandler)
                .requireCsrfProtectionMatcher(new OrRequestMatcher(
                        paths.matcher(HttpMethod.POST, "/api/auth/login"),
                        paths.matcher(HttpMethod.POST, "/api/auth/refresh"),
                        paths.matcher(HttpMethod.POST, "/api/auth/logout")
                )));

        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));
        http.formLogin(form -> form.disable());
        http.httpBasic(basic -> basic.disable());
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler));

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/api/auth/login",
                        "/api/auth/signup",
                        "/api/auth/refresh",
                        "/api/auth/logout",
                        "/api/auth/csrf"
                )
                .permitAll()
                .requestMatchers(
                        HttpMethod.GET,
                        "/api/categories",
                        "/api/posts",
                        "/api/posts/{postId}",
                        "/api/posts/{postId}/comments",
                        "/files/**"
                )
                .permitAll()
                .anyRequest()
                .authenticated());

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public SignedCsrfTokenRepository csrfTokenRepository() {
        return new SignedCsrfTokenRepository(csrfSecret, secure, sameSite);
    }

    @Bean
    public CsrfTokenRequestAttributeHandler csrfRequestHandler() {
        return new CsrfTokenRequestAttributeHandler();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint(ObjectMapper objectMapper) {
        return (request, response, exception) -> writeError(response, objectMapper, ErrorCode.UNAUTHORIZED);
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler(ObjectMapper objectMapper) {
        return (request, response, exception) -> {
            response.setStatus(403);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            ErrorResponse errorResponse = ErrorResponse.of(403, "접근이 거부되었습니다.");
            objectMapper.writeValue(response.getWriter(), errorResponse);
        };
    }

    private static void writeError(
            HttpServletResponse response,
            ObjectMapper objectMapper,
            ErrorCode errorCode
    ) throws IOException {
        response.setStatus(errorCode.getStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        ErrorResponse errorResponse = ErrorResponse.of(errorCode.getStatus(), errorCode.getMessage());
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        List<String> allowedOrigins = Arrays.stream(origins.split(","))
                .map(String::trim)
                .toList();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Content-Type", "Authorization", "X-XSRF-TOKEN"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AskTeacherUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }
}
