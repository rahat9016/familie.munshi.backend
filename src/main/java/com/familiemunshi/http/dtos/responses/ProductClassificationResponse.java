package com.familiemunshi.http.dtos.responses;

import com.familiemunshi.service.entities.ProductClassification;
import com.familiemunshi.service.enums.IconType;
import com.familiemunshi.service.enums.ProductClassificationLevel;
import lombok.Getter;

import java.util.UUID;

@Getter
public class ProductClassificationResponse {

    private UUID id;
    private String code;
    private String name;
    private String description;
    private ProductClassificationLevel level;
    private UUID parentId;
    private String parentName;

    /** FILE, LUCIDE or null when there is no icon. */
    private IconType iconType;

    /** lucide-react icon name when iconType = LUCIDE. */
    private String icon;

    /** Presigned S3 URL when iconType = FILE. */
    private String iconUrl;

    private Integer position;
    private boolean isActive;

    public ProductClassificationResponse(
            ProductClassification classification,
            String iconUrl
    ) {
        this.id = classification.id();
        this.code = classification.code();
        this.name = classification.name();
        this.description = classification.description();
        this.level = classification.level();
        this.parentId = classification.parentId();
        this.parentName = classification.parentName();
        this.iconType = classification.iconType();
        this.icon = classification.lucideIconName();
        this.iconUrl = iconUrl;
        this.position = classification.position();
        this.isActive = classification.isActive();
    }
}
