package com.familiemunshi.http.dtos.requests;

import com.familiemunshi.http.validations.ValidationUtil;
import com.familiemunshi.service.entities.ProductClassification;
import com.familiemunshi.service.enums.ProductClassificationLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Getter
@Setter
@NoArgsConstructor
@Schema(
        description = "Multipart request body for creating a new product classification"
)
public class CreateProductClassificationRequest {

    @Schema(
            description = "The name of the product classification",
            example = "Electronics",
            requiredMode = REQUIRED
    )
    @NotBlank
    @Size(max = 150)
    private String name;

    @Schema(
            description = "The level of the product classification",
            example = "CATEGORY",
            allowableValues = {"GROUP", "PARENT_CATEGORY", "SEGMENT", "CATEGORY", "SUB_CATEGORY"},
            requiredMode = REQUIRED
    )
    @NotNull
    private ProductClassificationLevel level;

    @Schema(
            description = "The ID of the parent product classification",
            example = "550e8400-e29b-41d4-a716-446655440000",
            requiredMode = NOT_REQUIRED
    )
    private UUID parentId;

    @Schema(
            description = "lucide-react icon name, e.g. 'shopping-cart' or 'ShoppingCart'. "
                    + "Send either icon or iconFile, not both.",
            example = "shopping-cart",
            requiredMode = NOT_REQUIRED
    )
    @Size(max = 100)
    @Pattern(
            regexp = "^$|" + ValidationUtil.LUCIDE_ICON_NAME_REGEX,
            message = "Icon must be a valid lucide-react icon name"
    )
    private String icon;

    @Schema(
            description = "Icon file uploaded to S3: SVG, PNG, WEBP or JPG, max 512 KB. "
                    + "Send either icon or iconFile, not both.",
            type = "string",
            format = "binary",
            requiredMode = NOT_REQUIRED
    )
    private MultipartFile iconFile;

    @Schema(
            description = "Display position within its level. Must be unique per level.",
            example = "1",
            requiredMode = REQUIRED
    )
    @NotNull
    @Positive
    private Integer position;

    @Schema(
            description = "Whether the product classification is active",
            example = "true",
            requiredMode = NOT_REQUIRED
    )
    private Boolean isActive = true;

    public ProductClassification toEntity() {
        return ProductClassification.builder()
                .name(this.name)
                .level(this.level)
                .parentId(this.parentId)
                .icon(this.icon)
                .position(this.position)
                .isActive(this.isActive == null || this.isActive)
                .build();
    }
}
