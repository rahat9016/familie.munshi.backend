package com.familiemunshi.http.dtos.requests;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@Schema(name = "AddColorwayImagesRequest", description = "Append images to a colorway. Max 10 images per colorway in total.")
public class AddColorwayImagesRequest {

    @ArraySchema(
            arraySchema = @Schema(description = "Image files (jpg, jpeg, png, webp). Appended after existing images.",
                    requiredMode = Schema.RequiredMode.REQUIRED),
            schema = @Schema(type = "string", format = "binary"),
            minItems = 1,
            maxItems = 10
    )
    @NotEmpty(message = "At least one image is required")
    private List<MultipartFile> images;
}
