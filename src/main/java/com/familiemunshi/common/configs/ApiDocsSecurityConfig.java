package com.familiemunshi.common.configs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security for the API documentation (Swagger UI + OpenAPI JSON).
 *
 * Runs BEFORE the main JWT filter chain and only matches the docs paths.
 *
 * docs.auth.enabled=false  -> docs are open (local development)
 * docs.auth.enabled=true   -> HTTP Basic login with docs.username / docs.password
 *                             (if credentials are missing, docs are locked: deny all)
 *
 * Whether the docs exist at all is controlled separately by
 * springdoc.api-docs.enabled / springdoc.swagger-ui.enabled.
 */
@Slf4j
@Configuration
public class ApiDocsSecurityConfig {

    private static final String[] DOCS_PATHS = {
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs",
            "/v3/api-docs/**"
    };

    @Value("${docs.auth.enabled:true}")
    private boolean authEnabled;

    @Value("${docs.username:}")
    private String username;

    @Value("${docs.password:}")
    private String password;

    @Bean
    @Order(1)
    public SecurityFilterChain apiDocsSecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher(DOCS_PATHS)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        if (!authEnabled) {
            return http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll()).build();
        }

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            log.warn("API docs authentication is enabled but DOCS_USERNAME/DOCS_PASSWORD are not set. "
                    + "API docs are locked.");
            return http.authorizeHttpRequests(auth -> auth.anyRequest().denyAll()).build();
        }

        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

        InMemoryUserDetailsManager docsUsers = new InMemoryUserDetailsManager(
                User.withUsername(username)
                        .password(encoder.encode(password))
                        .roles("API_DOCS")
                        .build()
        );

        return http
                .userDetailsService(docsUsers)
                .authorizeHttpRequests(auth -> auth.anyRequest().hasRole("API_DOCS"))
                .httpBasic(Customizer.withDefaults())
                .build();
    }
}
