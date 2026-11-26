package com.familiemunshi.service.entities;

import com.familiemunshi.service.enums.ProductClassificationLevel;
import lombok.Builder;
import lombok.With;

import java.util.UUID;


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
}