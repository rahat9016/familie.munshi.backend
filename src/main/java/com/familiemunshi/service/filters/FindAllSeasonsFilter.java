package com.familiemunshi.service.filters;

import com.familiemunshi.service.enums.SeasonStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

@Getter
@Setter
public class FindAllSeasonsFilter {

    @Schema(description = "Search by season name", requiredMode = NOT_REQUIRED)
    private String search;

    @Schema(
            description = "Filter by status",
            requiredMode = NOT_REQUIRED,
            allowableValues = {"Completed", "On Process", "On Concept", "On Planning"},
            example = "On Process"
    )
    private SeasonStatus status;
}
