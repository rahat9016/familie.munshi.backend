package com.familiemunshi.http.dtos.responses;

import com.familiemunshi.service.entities.Brand;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class BrandResponse {
    private UUID id;
    private String title;
    private String description;
    private String logoUrl;
    private Integer position;
    private boolean isActive;

    public BrandResponse(Brand brand, String logoUrl) {
        this.id = brand.id();
        this.title = brand.title();
        this.description = brand.description();
        this.logoUrl = logoUrl;
        this.position = brand.position();
        this.isActive = brand.isActive();
    }
}
