package com.familiemunshi.http.dtos.requests;

import com.familiemunshi.service.entities.Brand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Getter
@Setter
public class CreateBrandRequest {

    @Schema(description = "Brand title", requiredMode = REQUIRED, example = "Nike")
    @NotBlank(message = "Brand title is required")
    @Size(max = 150, message = "Brand title cannot exceed 150 characters")
    private String title;

    @Schema(description = "Brand description", requiredMode = NOT_REQUIRED)
    private String description;

    @Schema(description = "Brand logo image", type = "string", format = "binary", requiredMode = REQUIRED)
    @NotNull(message = "Brand logo is required")
    private MultipartFile logo;

    @Schema(description = "Display position. Must be unique across brands.", requiredMode = REQUIRED, example = "1")
    @NotNull
    @Positive
    private Integer position;

    @Schema(description = "Whether the brand is active", requiredMode = NOT_REQUIRED, example = "true")
    private Boolean isActive = true;

    public Brand toEntity() {
        return Brand.builder()
                .title(this.title)
                .description(this.description)
                .position(this.position)
                .isActive(this.isActive == null || this.isActive)
                .build();
    }
}
