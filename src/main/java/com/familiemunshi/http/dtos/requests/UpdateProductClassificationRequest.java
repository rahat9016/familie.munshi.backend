package com.familiemunshi.http.dtos.requests;

import com.familiemunshi.service.entities.ProductClassification;
import com.familiemunshi.service.enums.ProductClassificationLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;


import java.util.UUID;

@Schema(
        description = "The request body for updating a product classification"
)
@Getter
@Setter
@NoArgsConstructor
public class UpdateProductClassificationRequest
{
    @Schema(
            description = "The name of the product classification",
            example = "Electronics",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Size(min = 1, max = 255)
    String name;

    @Schema(
            description = "The level of the product classification",
            example = "CATEGORY",
            allowableValues = {"GROUP", "PARENT_CATEGORY", "CATEGORY", "SEGMENT", "SUB_CATEGORY"},
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    ProductClassificationLevel level;

    @Schema(
            description = "The ID of the parent product classification",
            example = "550e8400-e29b-41d4-a716-446655440000",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    UUID parentId;

    @Schema(
            description = "Icon name or URL. Omit to keep current icon, send empty string to remove it",
            example = "shirt",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Size(max = 100)
    String icon;

    @Schema(
            description = "Display position within its level. Must be unique per level. Omit to keep current position.",
            example = "1",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Positive
    Integer position;

    public ProductClassification toEntity() {
        return ProductClassification.builder()
                .name(this.name)
                .level(this.level)
                .parentId(this.parentId)
                .icon(this.icon)
                .position(this.position)
                .build();
    }
}
