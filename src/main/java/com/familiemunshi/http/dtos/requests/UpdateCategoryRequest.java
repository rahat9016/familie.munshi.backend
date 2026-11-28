package com.familiemunshi.http.dtos.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

@Getter
@Setter
@Schema(
        name = "UpdateCategoryRequest",
        description = "Partial update of a category. Omitted or null fields keep their current value."
)
public class UpdateCategoryRequest {

    @Schema(description = "Move to another department. Omit to keep current.", requiredMode = NOT_REQUIRED)
    private UUID departmentId;

    @Schema(
            description = "Category name. Omit or leave blank to keep current.",
            requiredMode = NOT_REQUIRED,
            maxLength = 100,
            example = "T-Shirts"
    )
    @Size(max = 100, message = "Category name cannot exceed 100 characters")
    private String name;

    @Schema(description = "Active flag. Omit to keep current.", requiredMode = NOT_REQUIRED, example = "false")
    private Boolean isActive;
}
