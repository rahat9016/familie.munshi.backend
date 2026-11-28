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
import static org.springframework.http.HttpMethod.*;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final ErrorUtils errorUtils;
    @Value("${cors.allowed-origins:http://localhost:3000, https://familiemunshi.de}")
    private String[] allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH"));
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
                        .requestMatchers("/ws/**").permitAll()

                        // =========== PUBLIC — landing / status page ===========
                        .requestMatchers(GET, "/", "/index.html", "/favicon.ico").permitAll()

                        // =========== PUBLIC — authentication ===========
                        .requestMatchers(POST, API_BASE + AUTH + "/login").permitAll()
                        .requestMatchers(POST, API_BASE + AUTH + "/signup").permitAll()
                        .requestMatchers(POST, API_BASE + AUTH + "/refresh-token").permitAll()
                        .requestMatchers(POST, API_BASE + AUTH + "/activate-branch-admin").permitAll()
                        .requestMatchers(POST, API_BASE + AUTH + LOGOUT).authenticated()

                        // User management endpoints
                        .requestMatchers(GET, API_BASE + USERS + "/me")
                        .authenticated()
                        .requestMatchers(API_BASE + USERS, API_BASE + USERS + "/**")
                        .authenticated()

                        // =========== PUBLIC — role management ===========
                        .requestMatchers(GET, API_BASE + SYSTEM + ROLES, API_BASE + SYSTEM + ROLES + "/**")
                        .authenticated().requestMatchers(POST, API_BASE + SYSTEM + ROLES)
                        .authenticated()
                        .requestMatchers( POST,API_BASE + SYSTEM + ROLES + "/*" + PERMISSIONS)
                        .authenticated()

                        .requestMatchers(
                                POST,
                                API_BASE + SYSTEM + USERS + "/*" + ROLES
                        )
                        .authenticated()

                        .requestMatchers(
                                DELETE,
                                API_BASE + SYSTEM + USERS + "/*" + ROLES + "/*"
                        )
                        .authenticated()

                        // =========== PERMISSION MANAGEMENT ===========
                        .requestMatchers(GET, API_BASE  + PERMISSIONS, API_BASE + PERMISSIONS + "/**").authenticated()

                        // =========== PRODUCT CLASSIFICATION ===========

                        .requestMatchers(API_BASE + PRODUCT_CLASSIFICATIONS + PRODUCT_CLASSIFICATION_ROOTS).permitAll()
                        .requestMatchers(API_BASE + PRODUCT_CLASSIFICATIONS + PRODUCT_CLASSIFICATION_TREE + "/**").permitAll()
                        // Public: active children of an active classification (storefront menus)
                        .requestMatchers(GET, API_BASE + PRODUCT_CLASSIFICATIONS + "/*" + PRODUCT_CLASSIFICATION_CHILDREN).permitAll()
                        .requestMatchers(GET, API_BASE + PRODUCT_CLASSIFICATIONS, API_BASE + PRODUCT_CLASSIFICATIONS + "/**")
                        .authenticated()
                        .requestMatchers(POST, API_BASE + PRODUCT_CLASSIFICATIONS + "/**").authenticated()
                        .requestMatchers(PATCH, API_BASE + PRODUCT_CLASSIFICATIONS + "/**").authenticated()
                        .requestMatchers(DELETE, API_BASE + PRODUCT_CLASSIFICATIONS + "/**").authenticated()

                        // =========== BRANDS ===========
                        .requestMatchers(GET, API_BASE + BRANDS + PUBLIC).permitAll()
                        .requestMatchers(API_BASE + BRANDS, API_BASE + BRANDS + "/**").authenticated()

                        // =========== COLORWAYS ===========
                        // Fine-grained COLORWAY_* checks enforced via @PreAuthorize in ColorwayController
                        .requestMatchers(GET, API_BASE + COLORWAYS, API_BASE + COLORWAYS + "/**").authenticated()
                        .requestMatchers(POST, API_BASE + COLORWAYS, API_BASE + COLORWAYS + "/**").authenticated()
                        .requestMatchers(PATCH, API_BASE + COLORWAYS + "/**").authenticated()
                        .requestMatchers(PUT, API_BASE + COLORWAYS + "/**").authenticated()
                        .requestMatchers(DELETE, API_BASE + COLORWAYS + "/**").authenticated()

                        // =========== SEASONS ===========
                        // Fine-grained SEASON_* checks enforced via @PreAuthorize in SeasonController
                        .requestMatchers(GET, API_BASE + SEASONS, API_BASE + SEASONS + "/**").authenticated()
                        .requestMatchers(POST, API_BASE + SEASONS).authenticated()
                        .requestMatchers(PATCH, API_BASE + SEASONS + "/**").authenticated()
                        .requestMatchers(DELETE, API_BASE + SEASONS + "/**").authenticated()

                        // =========== DEPARTMENTS ===========
                        // Fine-grained DEPARTMENT_* checks enforced via @PreAuthorize in DepartmentController
                        .requestMatchers(GET, API_BASE + DEPARTMENTS, API_BASE + DEPARTMENTS + "/**").authenticated()
                        .requestMatchers(POST, API_BASE + DEPARTMENTS).authenticated()
                        .requestMatchers(PATCH, API_BASE + DEPARTMENTS + "/**").authenticated()
                        .requestMatchers(DELETE, API_BASE + DEPARTMENTS + "/**").authenticated()

                        // =========== CATEGORIES ===========
                        // Fine-grained CATEGORY_* checks enforced via @PreAuthorize in CategoryController
                        .requestMatchers(GET, API_BASE + CATEGORIES, API_BASE + CATEGORIES + "/**").authenticated()
                        .requestMatchers(POST, API_BASE + CATEGORIES).authenticated()
                        .requestMatchers(PATCH, API_BASE + CATEGORIES + "/**").authenticated()
                        .requestMatchers(DELETE, API_BASE + CATEGORIES + "/**").authenticated()

                        // =========== PUBLIC — read-only branch browsing ===========
                        .requestMatchers(GET, API_BASE + BRANCHES)
                        .authenticated().requestMatchers(GET, API_BASE + BRANCHES + "/**")
                        .authenticated().requestMatchers(POST, API_BASE + BRANCHES + "/**")
                        .authenticated().requestMatchers(PATCH, API_BASE + BRANCHES + "/**")
                        .authenticated().requestMatchers("/error")
                        .permitAll())
                .addFilterBefore(filterApiRequest, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, exception) -> errorUtils.writeErrorResponse(response, HttpStatus.UNAUTHORIZED.value(), "UNAUTHORIZED", "Authentication required or invalid token"))
                        .accessDeniedHandler((request, response, exception) -> errorUtils.writeErrorResponse(response, HttpStatus.FORBIDDEN.value(), "FORBIDDEN", "You do not have permission"))).build();
    }
}
