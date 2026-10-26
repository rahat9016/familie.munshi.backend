package com.familiemunshi.service.entities;

import com.familiemunshi.service.enums.PermissionCode;
import lombok.Builder;
import lombok.With;

import java.time.Instant;
import java.util.UUID;

@Builder
@With
public record Permission(
        UUID id,
        PermissionCode code,
        String description,
        Instant createdAt,
        Instant updatedAt
) {}
