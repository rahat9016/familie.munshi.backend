package com.familiemunshi.service.entities;

import com.familiemunshi.common.utils.Constants;
import lombok.Builder;
import lombok.With;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@With
@Builder
public record Colorway(
        UUID id,
        String code,
        String marketingName,
        String name,
        String colorSpecification,
        String description,
        String colorStandard,
        String colorReference,
        String hexCode,
        boolean isActive,
        boolean inTheme,
        boolean sustainabilityLabel,
        boolean planSms,
        boolean plan3dSms,
        boolean actualSms,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate clearanceDate,
        List<ColorwayImage> images,
        Instant createdAt,
        Instant updatedAt
) {
    public Colorway {
        if (colorStandard == null || colorStandard.isBlank()) colorStandard = Constants.DEFAULT_COLOR_STANDARD;
        if (images == null) images = List.of();
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = Instant.now();
    }

    /** New colorway: only names given, everything else default. */
    public static Colorway newDraft(String marketingName, String name) {
        return Colorway.builder()
                .marketingName(marketingName.trim())
                .name(name.trim())
                .colorStandard(Constants.DEFAULT_COLOR_STANDARD)
                .isActive(true)
                .inTheme(false)
                .sustainabilityLabel(false)
                .planSms(false)
                .plan3dSms(false)
                .actualSms(false)
                .build();
    }
}
