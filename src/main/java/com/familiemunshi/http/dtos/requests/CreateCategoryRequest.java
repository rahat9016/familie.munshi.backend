package com.familiemunshi.http.dtos.requests;

import com.familiemunshi.service.entities.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Getter
@Setter
@Schema(
        name = "CreateCategoryRequest",
        description = "Create a category under a department. numberOfStyles is computed and cannot be set. "
                + "isActive defaults to true."
)
public class CreateCategoryRequest {

    @Schema(description = "Parent department id", requiredMode = REQUIRED)
    @NotNull(message = "Department id is required")
    private UUID departmentId;

    @Schema(
            description = "Category name, unique within its department (case-insensitive)",
            requiredMode = REQUIRED,
            maxLength = 100,
            example = "Shirts"
    )
    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name cannot exceed 100 characters")
    private String name;

    @Schema(description = "Indicates if the category is active", requiredMode = NOT_REQUIRED, example = "true")
    private Boolean isActive = true;

    public Category toEntity() {
        return Category.builder()
                .departmentId(this.departmentId)
                .name(this.name.trim())
                .isActive(Boolean.TRUE.equals(this.isActive))
                .build();
    }
}
