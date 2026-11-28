package com.familiemunshi.http.dtos.responses;

import com.familiemunshi.service.entities.Category;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class CategoryResponse {

    private final UUID id;
    private final UUID departmentId;
    private final String name;
    private final long numberOfStyles;
    private final boolean isActive;
    private final Instant createdAt;
    private final Instant updatedAt;

    public CategoryResponse(Category c, long numberOfStyles) {
        this.id = c.id();
        this.departmentId = c.departmentId();
        this.name = c.name();
        this.numberOfStyles = numberOfStyles;
        this.isActive = c.isActive();
        this.createdAt = c.createdAt();
        this.updatedAt = c.updatedAt();
    }
}
