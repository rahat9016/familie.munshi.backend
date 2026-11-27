package com.familiemunshi.http.dtos.requests;

import com.familiemunshi.http.validations.ValidationUtil;
import com.familiemunshi.service.entities.ProductClassification;
import com.familiemunshi.service.enums.ProductClassificationLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

@Getter
@Setter
@NoArgsConstructor
@Schema(
        description = "Multipart request body for updating a product classification"
)
public class UpdateProductClassificationRequest {

    @Schema(
            description = "The name of the product classification",
            example = "Electronics",
            requiredMode = NOT_REQUIRED
    )
    @Size(min = 1, max = 255)
    private String name;

    @Schema(
            description = "The level of the product classification",
            example = "CATEGORY",
            allowableValues = {"GROUP", "PARENT_CATEGORY", "SEGMENT", "CATEGORY", "SUB_CATEGORY"},
            requiredMode = NOT_REQUIRED
    )
    private ProductClassificationLevel level;

    @Schema(
            description = "The ID of the parent product classification",
            example = "550e8400-e29b-41d4-a716-446655440000",
            requiredMode = NOT_REQUIRED
    )
    private UUID parentId;

    @Schema(
            description = "lucide-react icon name. Omit to keep the current icon, "
                    + "send an empty string to remove it. Do not send together with iconFile.",
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
            description = "New icon file uploaded to S3: SVG, PNG, WEBP or JPG, max 512 KB. "
                    + "Replaces the current icon. Do not send together with icon.",
            type = "string",
            format = "binary",
            requiredMode = NOT_REQUIRED
    )
    private MultipartFile iconFile;

    @Schema(
            description = "Display position within its level. Must be unique per level. "
                    + "Omit to keep current position.",
            example = "1",
            requiredMode = NOT_REQUIRED
    )
    @Positive
    private Integer position;

    @Schema(
            description = "Whether the product classification is active. Omit to keep current.",
            example = "true",
            requiredMode = NOT_REQUIRED
    )
    private Boolean isActive;

    public ProductClassification toEntity() {
        return ProductClassification.builder()
                .name(this.name)
                .level(this.level)
                .parentId(this.parentId)
                .icon(this.icon)
                .position(this.position)
                .build();
    }
}
