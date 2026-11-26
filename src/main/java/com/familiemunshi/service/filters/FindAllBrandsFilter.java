package com.familiemunshi.service.filters;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindAllBrandsFilter {
    @Schema(description = "Search by brand title", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String search;

    @Schema(description = "Filter by active status", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean isActive;
}
