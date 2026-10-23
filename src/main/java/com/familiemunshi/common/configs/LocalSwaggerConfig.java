package com.familiemunshi.common.configs;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Bean;
import org.springdoc.core.models.GroupedOpenApi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import static com.familiemunshi.common.utils.Constants.*;

@Configuration
@Profile("local")
public class LocalSwaggerConfig {
    @Bean
    public GroupedOpenApi publicApis() {
        return GroupedOpenApi.builder()
                .group("Public APIs")
                .pathsToMatch(API_BASE + "/**")
                .build();
    }

    @Bean
    public GroupedOpenApi internalApis() {
        return GroupedOpenApi.builder()
                .group("Internal APIs")
                .pathsToMatch(API_BASE + "/internal/**")
                .build();
    }


    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(APP_NAME)
                        .description("Familie Munshi API Documentation")
                        .version("v1.0.0")
                )
                .components(new Components()
                        .addSecuritySchemes(
                                "bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                )
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList("bearerAuth")
                );
    }
}
