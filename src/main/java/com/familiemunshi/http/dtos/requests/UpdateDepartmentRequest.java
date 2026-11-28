package com.familiemunshi.http.dtos.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

@Getter
@Setter
@Schema(
        name = "UpdateDepartmentRequest",
        description = "Partial update of a department. Omitted or null fields keep their current value."
)
public class UpdateDepartmentRequest {

    @Schema(
            description = "Department name. Omit or leave blank to keep current.",
            requiredMode = NOT_REQUIRED,
            maxLength = 100,
            example = "Womenswear"
    )
    @Size(max = 100, message = "Department name cannot exceed 100 characters")
    private String name;

    @Schema(description = "Active flag. Omit to keep current.", requiredMode = NOT_REQUIRED, example = "false")
    private Boolean isActive;
}
