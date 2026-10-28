package com.familiemunshi.service.filters;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindAllPermissionsFilter {
    @Schema(
            description = "Search by permission code or description",
            example = "role"
    )
    private String search;

    @Schema(
            description = "Filter by permission module",
            example = "ROLE"
    )
    private String module;
}
