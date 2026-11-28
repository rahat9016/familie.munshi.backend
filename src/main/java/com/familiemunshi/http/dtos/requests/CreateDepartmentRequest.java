package com.familiemunshi.http.dtos.requests;

import com.familiemunshi.service.entities.Department;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Getter
@Setter
@Schema(
        name = "CreateDepartmentRequest",
        description = "Create a department. numberOfStyles is computed and cannot be set. "
                + "isActive defaults to true."
)
public class CreateDepartmentRequest {

    @Schema(
            description = "Department name, unique (case-insensitive)",
            requiredMode = REQUIRED,
            maxLength = 100,
            example = "Menswear"
    )
    @NotBlank(message = "Department name is required")
    @Size(max = 100, message = "Department name cannot exceed 100 characters")
    private String name;

    @Schema(description = "Indicates if the department is active", requiredMode = NOT_REQUIRED, example = "true")
    private Boolean isActive = true;

    public Department toEntity() {
        return Department.builder()
                .name(this.name.trim())
                .isActive(Boolean.TRUE.equals(this.isActive))
                .build();
    }
}
