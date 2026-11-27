package com.familiemunshi.http.dtos.responses;

import com.familiemunshi.service.entities.Colorway;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
public class ColorwayResponse {

    private final UUID id;
    private final String code;
    private final String marketingName;
    private final String name;
    private final String colorSpecification;
    private final String description;
    private final String colorStandard;
    private final String colorReference;
    private final String hexCode;
    private final boolean isActive;
    private final boolean inTheme;
    private final boolean sustainabilityLabel;
    private final boolean planSms;
    private final boolean plan3dSms;
    private final boolean actualSms;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final LocalDate clearanceDate;
    private final int imageCount;
    private final List<ImageResponse> images;
    private final Instant createdAt;
    private final Instant updatedAt;

    public record ImageResponse(UUID id, String url, Integer position) {
    }

    public ColorwayResponse(Colorway c, List<ImageResponse> images) {
        this.id = c.id();
        this.code = c.code();
        this.marketingName = c.marketingName();
        this.name = c.name();
        this.colorSpecification = c.colorSpecification();
        this.description = c.description();
        this.colorStandard = c.colorStandard();
        this.colorReference = c.colorReference();
        this.hexCode = c.hexCode();
        this.isActive = c.isActive();
        this.inTheme = c.inTheme();
        this.sustainabilityLabel = c.sustainabilityLabel();
        this.planSms = c.planSms();
        this.plan3dSms = c.plan3dSms();
        this.actualSms = c.actualSms();
        this.startDate = c.startDate();
        this.endDate = c.endDate();
        this.clearanceDate = c.clearanceDate();
        this.imageCount = images.size();
        this.images = images;
        this.createdAt = c.createdAt();
        this.updatedAt = c.updatedAt();
    }
}
