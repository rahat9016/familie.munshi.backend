package com.familiemunshi.service.entities;

import com.familiemunshi.service.enums.IconType;
import com.familiemunshi.service.enums.ProductClassificationLevel;
import lombok.Builder;
import lombok.With;

import java.util.UUID;

/*
 * icon holds one of:
 *   null                                  -> no icon
 *   "public/product-classifications/..."  -> uploaded file (S3 key)
 *   "shopping-cart" / "ShoppingCart"      -> lucide-react icon name
 * Lucide names never contain '/', so the prefix check is unambiguous.
 */
@With
@Builder
public record ProductClassification(
        UUID id,
        String code,
        String name,
        ProductClassificationLevel level,
        UUID parentId,
        String parentName,
        String icon,
        Integer position,
        boolean isActive
) {

    public static final String UPLOADED_ICON_PREFIX = "public/";

    public static boolean isUploadedIcon(String icon) {
        return icon != null && icon.startsWith(UPLOADED_ICON_PREFIX);
    }

    public boolean hasUploadedIcon() {
        return isUploadedIcon(icon);
    }

    public IconType iconType() {
        if (icon == null || icon.isBlank()) {
            return null;
        }
        return hasUploadedIcon() ? IconType.FILE : IconType.LUCIDE;
    }

    /** lucide-react name, or null when the icon is an uploaded file. */
    public String lucideIconName() {
        return iconType() == IconType.LUCIDE ? icon : null;
    }
}
