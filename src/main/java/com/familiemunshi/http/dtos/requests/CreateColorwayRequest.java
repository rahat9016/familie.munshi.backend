package com.familiemunshi.http.dtos.requests;

import com.familiemunshi.service.entities.Colorway;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Getter
@Setter
@Schema(
        name = "CreateColorwayRequest",
        description = "Create a colorway. Only marketing name and colorway name are required. "
                + "Code is auto-generated (CLR-XXXXXX), colorStandard defaults to PANTONE, "
                + "isActive defaults to true, all other flags default to false and remaining fields to null."
)
public class CreateColorwayRequest {

    @Schema(
            description = "Color marketing name",
            requiredMode = REQUIRED,
            maxLength = 150,
            example = "Midnight Navy"
    )
    @NotBlank(message = "Color marketing name is required")
    @Size(max = 150, message = "Color marketing name cannot exceed 150 characters")
    private String marketingName;

    @Schema(
            description = "Colorway name",
            requiredMode = REQUIRED,
            maxLength = 150,
            example = "Navy/White"
    )
    @NotBlank(message = "Colorway is required")
    @Size(max = 150, message = "Colorway cannot exceed 150 characters")
    private String name;

    public Colorway toEntity() {
        return Colorway.newDraft(marketingName, name);
    }
}
