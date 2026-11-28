package com.familiemunshi.http.dtos.requests;

import com.familiemunshi.service.enums.SeasonStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

@Getter
@Setter
@Schema(
        name = "UpdateSeasonRequest",
        description = "Partial update of a season. Omitted or null fields keep their current value."
)
public class UpdateSeasonRequest {

    @Schema(
            description = "Season name. Omit or leave blank to keep current.",
            requiredMode = NOT_REQUIRED,
            maxLength = 100,
            example = "SS27"
    )
    @Size(max = 100, message = "Season cannot exceed 100 characters")
    private String season;

    @Schema(
            description = "Season status. Omit to keep current.",
            requiredMode = NOT_REQUIRED,
            allowableValues = {"Completed", "On Process", "On Concept", "On Planning"},
            example = "On Concept"
    )
    private SeasonStatus status;
}
