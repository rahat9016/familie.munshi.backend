package com.familiemunshi.http.dtos.responses;

import com.familiemunshi.service.enums.IconType;

import java.util.List;
import java.util.UUID;

public record ProductClassificationTreeResponse(
        UUID id,
        String name,
        String description,
        IconType iconType,
        String icon,
        String iconUrl,
        Integer position,
        List<ProductClassificationTreeResponse> children
) {}
