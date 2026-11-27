package com.familiemunshi.service.entities;

import lombok.Builder;

import java.util.UUID;

@Builder
public record ColorwayImage(UUID id, String imageKey, Integer position) {
}
