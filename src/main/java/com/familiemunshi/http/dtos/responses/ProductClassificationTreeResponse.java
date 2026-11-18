package com.familiemunshi.http.dtos.responses;

import com.familiemunshi.service.enums.ProductClassificationLevel;

import java.util.List;
import java.util.UUID;

public record ProductClassificationTreeResponse(
        UUID id,
        String code,
        String name,
        ProductClassificationLevel level,
        List<ProductClassificationTreeResponse> children
) {}
