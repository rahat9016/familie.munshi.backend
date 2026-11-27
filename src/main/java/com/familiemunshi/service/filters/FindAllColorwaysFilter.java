package com.familiemunshi.service.filters;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

@Getter
@Setter
public class FindAllColorwaysFilter {

    @Schema(description = "Search by code, colorway or marketing name", requiredMode = NOT_REQUIRED)
    private String search;

    @Schema(description = "Filter by code (partial, case-insensitive)", requiredMode = NOT_REQUIRED, example = "CLR-ZW")
    private String code;

    @Schema(description = "Filter by color standard (case-insensitive)", requiredMode = NOT_REQUIRED, example = "PANTONE")
    private String colorStandard;

    @Schema(description = "Filter by active status", requiredMode = NOT_REQUIRED)
    private Boolean isActive;

    @Schema(description = "Filter by in-theme flag", requiredMode = NOT_REQUIRED)
    private Boolean inTheme;
}
