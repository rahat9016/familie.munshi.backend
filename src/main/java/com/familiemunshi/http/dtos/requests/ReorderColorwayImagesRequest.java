package com.familiemunshi.http.dtos.requests;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Schema(name = "ReorderColorwayImagesRequest", description = "New image order. Must list every image of the colorway exactly once. First image becomes cover.")
public class ReorderColorwayImagesRequest {

    @ArraySchema(
            arraySchema = @Schema(description = "Image IDs in desired order", requiredMode = Schema.RequiredMode.REQUIRED),
            schema = @Schema(type = "string", format = "uuid", example = "3f2b8c1e-9a4d-4e7b-8c21-5d6f7a8b9c0d"),
            minItems = 1
    )
    @NotEmpty(message = "Image order is required")
    private List<UUID> imageIds;
}
