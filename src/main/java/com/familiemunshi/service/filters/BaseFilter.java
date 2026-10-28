package com.familiemunshi.service.filters;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseFilter {

    @Schema(
            description = "Search records",
            example = "admin"
    )
    private String search;

    @Schema(
            description = "Filter by active status",
            example = "true"
    )
    private Boolean isActive;
}
