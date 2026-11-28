package com.familiemunshi.service.filters;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

@Getter
@Setter
public class FindAllCategoriesFilter {

    @Schema(description = "Search by category name", requiredMode = NOT_REQUIRED)
    private String search;

    @Schema(description = "Filter by parent department id", requiredMode = NOT_REQUIRED)
    private UUID departmentId;

    @Schema(description = "Filter by active flag", requiredMode = NOT_REQUIRED, example = "true")
    private Boolean isActive;
}
