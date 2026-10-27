package com.familiemunshi.service.filters;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindAllRolesFilter {

    @Schema(
            description = "Search roles by name or description",
            example = "admin"
    )
    private String search;

    @Schema(
            description = "Filter by active or inactive status",
            example = "true"
    )
    private Boolean isActive;

    @Schema(
            description = "Filter system or custom roles",
            example = "false"
    )
    private Boolean isSystem;
}
