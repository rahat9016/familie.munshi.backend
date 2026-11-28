package com.familiemunshi.http.dtos.responses;

import com.familiemunshi.service.entities.Department;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
public class DepartmentResponse {

    private final UUID id;
    private final String name;
    private final long numberOfStyles;
    private final boolean isActive;
    private final List<String> categories; // category names only
    private final Instant createdAt;
    private final Instant updatedAt;

    public DepartmentResponse(Department d, long numberOfStyles, List<String> categories) {
        this.id = d.id();
        this.name = d.name();
        this.numberOfStyles = numberOfStyles;
        this.isActive = d.isActive();
        this.categories = categories;
        this.createdAt = d.createdAt();
        this.updatedAt = d.updatedAt();
    }
}
