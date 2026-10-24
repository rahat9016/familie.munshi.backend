package com.familiemunshi.service.entities;

import com.familiemunshi.service.enums.RoleScope;
import lombok.Builder;
import lombok.With;

import java.time.Instant;
import java.util.UUID;

@With
@Builder
public record Role(
        UUID  id,
        String name,
        String description,
        Boolean isSystem,
        RoleScope scope,
        Boolean isActive,
        Branch branch,
        Instant createdAt,
        Instant updatedAt
) {}
