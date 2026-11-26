package com.familiemunshi.service.entities;

import lombok.Builder;
import lombok.With;

import java.time.Instant;
import java.util.UUID;

@With
@Builder
public record Brand(
        UUID id,
        String title,
        String description,
        String logoUrl,
        Integer position,
        boolean isActive,
        Instant createdAt,
        Instant updatedAt
) {
    public Brand {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (updatedAt == null) {
            updatedAt = Instant.now();
        }
    }
}
