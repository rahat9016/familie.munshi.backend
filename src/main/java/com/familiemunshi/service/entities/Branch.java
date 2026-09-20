package com.familiemunshi.service.entities;

import lombok.*;

@With
@Builder
public record Branch (
    Long id,
    String name,
    String address,
    String logoUrl,
    boolean isActive
) {
}
