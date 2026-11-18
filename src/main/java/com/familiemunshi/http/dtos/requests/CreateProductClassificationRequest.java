package com.familiemunshi.http.dtos.requests;

import com.familiemunshi.service.entities.ProductClassification;
import com.familiemunshi.service.entities.User;
import com.familiemunshi.service.enums.ProductClassificationLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@Schema(
        description = "The request body for creating a new product classification"
)
public class CreateProductClassificationRequest {
    @Schema(
            description = "The name of the product classification",
            example = "Electronics",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank
    String name;

    @Schema(
            description = "The level of the product classification",
            example = "CATEGORY",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull
    ProductClassificationLevel level;

    @Schema(
            description = "The ID of the parent product classification",
            example = "550e8400-e29b-41d4-a716-446655440000",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    UUID parentId;

    public ProductClassification toEntity() {
        return ProductClassification.builder()
                .name(this.name)
                .level(this.level)
                .parentId(this.parentId)
                .build();
    }
}
