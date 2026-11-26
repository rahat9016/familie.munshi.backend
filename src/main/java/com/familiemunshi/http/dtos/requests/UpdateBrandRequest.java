package com.familiemunshi.http.dtos.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

@Getter
@Setter
public class UpdateBrandRequest {

    @Schema(description = "Brand title. Omit or leave blank to keep current.", requiredMode = NOT_REQUIRED, example = "Nike")
    @Size(max = 150)
    private String title;

    @Schema(description = "Brand description", requiredMode = NOT_REQUIRED)
    private String description;

    @Schema(description = "Brand logo image. Omit to keep current.", type = "string", format = "binary", requiredMode = NOT_REQUIRED)
    private MultipartFile logo;

    @Schema(description = "Display position. Unique across brands. Omit to keep current.", requiredMode = NOT_REQUIRED, example = "1")
    @Positive
    private Integer position;

    @Schema(description = "Whether the brand is active. Omit to keep current.", requiredMode = NOT_REQUIRED, example = "true")
    private Boolean isActive;
}
