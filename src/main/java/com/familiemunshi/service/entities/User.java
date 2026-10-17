package com.familiemunshi.service.entities;

import lombok.Builder;
import lombok.With;

import java.time.Instant;
import java.util.UUID;


@Builder
@With
public record User(
        UUID id,
        String profilePicture,
        String firstName,
        String lastName,
        String email,
        String password,
        boolean isActive,
        Instant createdAt,
        Instant updatedAt
) {
}
