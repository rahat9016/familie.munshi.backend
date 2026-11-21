package com.familiemunshi.http.dtos.responses;

import java.util.List;
import java.util.UUID;

public record ProductClassificationTreeResponse(
        UUID id,
        String name,
        String icon,
        List<ProductClassificationTreeResponse> children
) {}
