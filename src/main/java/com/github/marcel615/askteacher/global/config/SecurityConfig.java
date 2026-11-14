package com.github.marcel615.askteacher.global.config;

import com.github.marcel615.askteacher.domain.auth.service.AuthService;
import com.github.marcel615.askteacher.global.security.AskTeacherAccessDeniedHandler;
import com.github.marcel615.askteacher.global.security.AskTeacherAuthenticationEntryPoint;
import com.github.marcel615.askteacher.global.security.AskTeacherUserDetailsService;
import com.github.marcel615.askteacher.global.security.SecurityResponseWriter;
import com.github.marcel615.askteacher.global.security.SignedCsrfTokenRepository;
import com.github.marcel615.askteacher.global.security.jwt.JwtAuthenticationFilter;
import com.github.marcel615.askteacher.global.security.jwt.JwtLoginFailureHandler;
import com.github.marcel615.askteacher.global.security.jwt.JwtLoginFilter;
import com.github.marcel615.askteacher.global.security.jwt.JwtLoginSuccessHandler;
import com.github.marcel615.askteacher.global.security.jwt.JwtLogoutFilter;
import com.github.marcel615.askteacher.global.security.jwt.JwtLogoutHandler;
import com.github.marcel615.askteacher.global.security.jwt.RefreshTokenCookieProvider;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
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
import tools.jackson.databind.json.JsonMapper;

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
            AccessDeniedHandler accessDeniedHandler,
            AuthenticationManager authenticationManager,
            JsonMapper objectMapper,
            Validator validator,
            JwtLoginSuccessHandler loginSuccessHandler,
            JwtLoginFailureHandler loginFailureHandler,
            JwtLogoutHandler logoutHandler,
            RefreshTokenCookieProvider cookies,
            SecurityResponseWriter responses
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
        http.logout(logout -> logout.disable());
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
        JwtLoginFilter loginFilter = new JwtLoginFilter(
                authenticationManager,
                new JacksonJsonHttpMessageConverter(objectMapper),
                validator,
                loginSuccessHandler,
                loginFailureHandler,
                responses
        );
        JwtLogoutFilter logoutFilter = new JwtLogoutFilter(logoutHandler, cookies, responses);
        http.addFilterAfter(loginFilter, JwtAuthenticationFilter.class);
        http.addFilterAfter(logoutFilter, JwtLoginFilter.class);
        return http.build();
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration() {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(jwtAuthenticationFilter);
        registration.setEnabled(false);
        return registration;
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
    public SecurityResponseWriter securityResponseWriter(ObjectMapper objectMapper) {
        return new SecurityResponseWriter(objectMapper);
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint(SecurityResponseWriter responses) {
        return new AskTeacherAuthenticationEntryPoint(responses);
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler(SecurityResponseWriter responses) {
        return new AskTeacherAccessDeniedHandler(responses);
    }

    @Bean
    public JwtLoginSuccessHandler loginSuccessHandler(
            AuthService authService,
            RefreshTokenCookieProvider cookies,
            SecurityResponseWriter responses
    ) {
        return new JwtLoginSuccessHandler(authService, cookies, responses);
    }

    @Bean
    public JwtLoginFailureHandler loginFailureHandler(SecurityResponseWriter responses) {
        return new JwtLoginFailureHandler(responses);
    }

    @Bean
    public JwtLogoutHandler logoutHandler(AuthService authService, RefreshTokenCookieProvider cookies) {
        return new JwtLogoutHandler(authService, cookies);
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
