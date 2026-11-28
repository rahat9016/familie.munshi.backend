package com.familiemunshi.service.entities;

import lombok.Builder;
import lombok.With;

import java.time.Instant;
import java.util.UUID;

@With
@Builder
public record Department(
        UUID id,
        String name,
        boolean isActive,
        Instant createdAt,
        Instant updatedAt
) {}
