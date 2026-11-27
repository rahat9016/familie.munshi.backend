package com.familiemunshi.http.dtos.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

@Getter
@Setter
@Schema(
        name = "UpdateColorwayRequest",
        description = "Partial update of a colorway. Every field is optional; omitted or null fields keep their current value. "
                + "Code cannot be changed. Images are managed through the /{id}/images endpoints."
)
public class UpdateColorwayRequest {

    @Schema(
            description = "Color marketing name. Omit or leave blank to keep current.",
            requiredMode = NOT_REQUIRED,
            maxLength = 150,
            example = "Midnight Navy"
    )
    @Size(max = 150, message = "Color marketing name cannot exceed 150 characters")
    private String marketingName;

    @Schema(
            description = "Colorway name. Omit or leave blank to keep current.",
            requiredMode = NOT_REQUIRED,
            maxLength = 150,
            example = "Navy/White"
    )
    @Size(max = 150, message = "Colorway cannot exceed 150 characters")
    private String name;

    @Schema(
            description = "Color specification. Omit to keep current.",
            requiredMode = NOT_REQUIRED,
            maxLength = 255,
            example = "Solid dyed, matte finish"
    )
    @Size(max = 255, message = "Color specification cannot exceed 255 characters")
    private String colorSpecification;

    @Schema(
            description = "Free-text description. Omit to keep current.",
            requiredMode = NOT_REQUIRED,
            example = "Core navy for AW26 outerwear"
    )
    private String description;

    @Schema(
            description = "Color standard. Stored uppercase. Defaults to PANTONE on create. Omit or leave blank to keep current.",
            requiredMode = NOT_REQUIRED,
            maxLength = 20,
            example = "PANTONE"
    )
    @Size(max = 20, message = "Color standard cannot exceed 20 characters")
    private String colorStandard;

    @Schema(
            description = "Color reference within the standard (e.g. Pantone code). Send empty string to clear.",
            requiredMode = NOT_REQUIRED,
            maxLength = 50,
            example = "19-4052 TCX"
    )
    @Size(max = 50, message = "Color reference cannot exceed 50 characters")
    private String colorReference;

    @Schema(
            description = "Hex color in #RRGGBB format. Stored uppercase. Omit to keep current.",
            requiredMode = NOT_REQUIRED,
            pattern = "^#[0-9A-Fa-f]{6}$",
            example = "#1B2A4A"
    )
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Hex must be in #RRGGBB format")
    private String hexCode;

    @Schema(
            description = "Whether the colorway is active. Omit to keep current.",
            requiredMode = NOT_REQUIRED,
            example = "true"
    )
    private Boolean isActive;

    @Schema(
            description = "Whether the colorway is part of the current theme. Omit to keep current.",
            requiredMode = NOT_REQUIRED,
            example = "false"
    )
    private Boolean inTheme;

    @Schema(
            description = "Sustainability label on/off. Omit to keep current.",
            requiredMode = NOT_REQUIRED,
            example = "false"
    )
    private Boolean sustainabilityLabel;

    @Schema(
            description = "Planned salesman sample (SMS). Omit to keep current.",
            requiredMode = NOT_REQUIRED,
            example = "false"
    )
    private Boolean planSms;

    @Schema(
            description = "Planned 3D salesman sample (3D SMS). Omit to keep current.",
            requiredMode = NOT_REQUIRED,
            example = "false"
    )
    private Boolean plan3dSms;

    @Schema(
            description = "Actual salesman sample (SMS) produced. Omit to keep current.",
            requiredMode = NOT_REQUIRED,
            example = "false"
    )
    private Boolean actualSms;

    @Schema(
            description = "Planning start date (ISO yyyy-MM-dd). Omit to keep current.",
            requiredMode = NOT_REQUIRED,
            type = "string",
            format = "date",
            example = "2026-12-01"
    )
    private LocalDate startDate;

    @Schema(
            description = "Planning end date (ISO yyyy-MM-dd). Must be on or after start date. Omit to keep current.",
            requiredMode = NOT_REQUIRED,
            type = "string",
            format = "date",
            example = "2027-03-31"
    )
    private LocalDate endDate;

    @Schema(
            description = "Clearance date (ISO yyyy-MM-dd). Must be on or after end date. Omit to keep current.",
            requiredMode = NOT_REQUIRED,
            type = "string",
            format = "date",
            example = "2027-05-15"
    )
    private LocalDate clearanceDate;
}
