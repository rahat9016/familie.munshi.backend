package com.familiemunshi.http.dtos.responses;

import com.familiemunshi.service.enums.ProductClassificationLevel;

import java.util.UUID;

public record ProductClassificationResponse(
        UUID id,
        String code,
        String name,
        ProductClassificationLevel level,
        UUID parentId,
        boolean isActive
) {}