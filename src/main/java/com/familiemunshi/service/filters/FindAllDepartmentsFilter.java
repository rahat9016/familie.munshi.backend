package com.familiemunshi.service.filters;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

@Getter
@Setter
public class FindAllDepartmentsFilter {

    @Schema(description = "Search by department name", requiredMode = NOT_REQUIRED)
    private String search;

    @Schema(description = "Filter by active flag", requiredMode = NOT_REQUIRED, example = "true")
    private Boolean isActive;
}
