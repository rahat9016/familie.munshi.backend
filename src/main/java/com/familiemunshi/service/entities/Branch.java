package com.familiemunshi.service.entities;

import lombok.*;

import java.time.Instant;

@With
@Builder
public record Branch (
    Long id,
    String name,
    String address,
    String phone,
    String logoUrl,
    boolean isActive,
    String code,
    Instant createdAt,
    Instant updatedAt
) {
    public Branch {
        if(createdAt == null) {
            createdAt = Instant.now();
        }
        if(updatedAt == null) {
            updatedAt = Instant.now();
        }
    }
}
