package com.familiemunshi.common.configs;


import com.familiemunshi.common.component.FilterApiRequest;
import com.familiemunshi.common.exceptions.ErrorUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static com.familiemunshi.common.utils.Constants.*;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    @Value("${cors.allowed-origins:http://localhost:3000, https://familiemunshi.com}")
    private String[] allowedOrigins;

    private final ErrorUtils errorUtils;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http, FilterApiRequest filterApiRequest) throws Exception {
        return http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).csrf(AbstractHttpConfigurer::disable).cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // =========== PUBLIC — infrastructure & API docs ===========
                .authorizeHttpRequests(authorize -> authorize
                        // =========== PUBLIC — infrastructure & API docs ===========
                        .requestMatchers(API_DOCS, SWAGGER_UI, "/api-docs-ui/**", "/ws/**")
                        .permitAll()

                        // =========== PUBLIC — authentication ===========
                        .requestMatchers(POST, API_BASE + AUTH + "/login")
                        .permitAll()
                        .requestMatchers(POST, API_BASE + AUTH + "/signup")
                        .permitAll().requestMatchers(POST, API_BASE + AUTH + "/refresh-token")
                        .permitAll()

                        // User management endpoints
                        .requestMatchers(GET, API_BASE + USERS + "/me")
                        .authenticated()
                        .requestMatchers(API_BASE + USERS, API_BASE + USERS + "/**")
                        .denyAll()

                        // =========== PUBLIC — role management ===========
                        .requestMatchers(POST, API_BASE + SYSTEM + ROLES)
                        .permitAll()
                        .requestMatchers(GET, API_BASE + SYSTEM + ROLES + "/**")
                        .permitAll()

                        // =========== PUBLIC — read-only branch browsing ===========
                        .requestMatchers(GET, API_BASE + BRANCHES).permitAll()
                        .requestMatchers(GET, API_BASE + BRANCHES + "/**")
                        .permitAll()
                        .requestMatchers(POST, API_BASE + BRANCHES + "/**")
                        .permitAll()
                        .requestMatchers("/error")
                        .permitAll())
                .addFilterBefore(filterApiRequest, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, exception) ->
                                errorUtils.writeErrorResponse(
                                        response,
                                        HttpStatus.UNAUTHORIZED.value(),
                                        "UNAUTHORIZED",
                                        "Authentication required or invalid token"
                                )
                        )
                        .accessDeniedHandler((request, response, exception) ->
                                errorUtils.writeErrorResponse(
                                        response,
                                        HttpStatus.FORBIDDEN.value(),
                                        "FORBIDDEN",
                                        "You do not have permission"
                                )
                        )
                )
                .build();
    }
}
