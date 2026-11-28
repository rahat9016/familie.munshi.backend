package com.familiemunshi.service.entities;

import com.familiemunshi.service.enums.SeasonStatus;
import lombok.Builder;
import lombok.With;

import java.time.Instant;
import java.util.UUID;

@With
@Builder
public record Season(
        UUID id,
        String season,
        SeasonStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public Season {
        if (status == null) status = SeasonStatus.ON_PLANNING;
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = Instant.now();
    }

    /** New season: name required, status defaults to ON_PLANNING. */
    public static Season newDraft(String season, SeasonStatus status) {
        return Season.builder()
                .season(season.trim())
                .status(status)
                .build();
    }
}
