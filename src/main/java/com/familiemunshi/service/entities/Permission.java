package com.familiemunshi.service.entities;

import lombok.Builder;
import lombok.With;

@Builder
@With
public record Permission(
        String name,
        String description
) {}
