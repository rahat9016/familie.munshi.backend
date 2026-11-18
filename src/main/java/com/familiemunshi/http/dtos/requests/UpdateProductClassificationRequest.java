package com.familiemunshi.http.dtos.requests;

import com.familiemunshi.service.enums.ProductClassificationLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(
        description = "The request body for updating a product classification"
)
public class UpdateProductClassificationRequest
{
    @Schema(
            description = "The code of the product classification",
            example = "ELECTRONICS",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Size(min = 1, max = 100)
    String code;

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
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    ProductClassificationLevel level;

    @Schema(
            description = "The ID of the parent product classification",
            example = "550e8400-e29b-41d4-a716-446655440000",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    UUID parentId;

}
