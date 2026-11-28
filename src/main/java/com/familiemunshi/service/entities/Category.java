package com.familiemunshi.service.entities;

import lombok.Builder;
import lombok.With;

import java.time.Instant;
import java.util.UUID;

@With
@Builder
public record Category(
        UUID id,
        UUID departmentId,
        String name,
        boolean isActive,
        Instant createdAt,
        Instant updatedAt
) {}
