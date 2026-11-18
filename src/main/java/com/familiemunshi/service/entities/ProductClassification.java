package com.familiemunshi.service.entities;

import com.familiemunshi.service.enums.ProductClassificationLevel;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

public record ProductClassification(
        UUID id,
        String code,
        String name,
        ProductClassificationLevel level,
        UUID parentId,
        boolean isActive
) {
}