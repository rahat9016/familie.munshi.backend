package com.familiemunshi.http.dtos.requests;

import com.familiemunshi.service.entities.Season;
import com.familiemunshi.service.enums.SeasonStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Getter
@Setter
@Schema(
        name = "CreateSeasonRequest",
        description = "Create a season. numberOfStyles is computed and cannot be set. "
                + "Status defaults to 'On Planning'."
)
public class CreateSeasonRequest {

    @Schema(
            description = "Season name, unique (case-insensitive)",
            requiredMode = REQUIRED,
            maxLength = 100,
            example = "AW26"
    )
    @NotBlank(message = "Season is required")
    @Size(max = 100, message = "Season cannot exceed 100 characters")
    private String season;

    @Schema(
            description = "Season status",
            requiredMode = NOT_REQUIRED,
            allowableValues = {"Completed", "On Process", "On Concept", "On Planning"},
            example = "On Planning"
    )
    private SeasonStatus status;

    public Season toEntity() {
        return Season.newDraft(season, status);
    }
}
