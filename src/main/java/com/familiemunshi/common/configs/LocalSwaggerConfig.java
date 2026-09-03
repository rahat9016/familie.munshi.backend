package com.familiemunshi.common.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Bean;
import org.springdoc.core.models.GroupedOpenApi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import static com.familiemunshi.common.utils.Constands.APP_NAME;


@Configuration
@Profile("local")
public class LocalSwaggerConfig {
    @Bean
    public GroupedOpenApi publicApis() {
        return GroupedOpenApi.builder()
                .group("Public APIs")
                .pathsToMatch("/api/public/**")
                .build();
    }

    @Bean
    public GroupedOpenApi internalApis() {
        return GroupedOpenApi.builder()
                .group("Internal APIs")
                .pathsToMatch("/api/internal/**")
                .build();
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info().title(APP_NAME)
                .description("Familie Munshi API Documentation")
                .version("v1.0.0"));
    }
}
