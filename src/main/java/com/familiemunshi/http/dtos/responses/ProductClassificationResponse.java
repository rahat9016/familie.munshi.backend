package com.familiemunshi.http.dtos.responses;

import com.familiemunshi.service.entities.ProductClassification;
import com.familiemunshi.service.enums.ProductClassificationLevel;
import lombok.Getter;

import java.util.UUID;

@Getter
public class ProductClassificationResponse {

    private UUID id;
    private String code;
    private String name;
    private ProductClassificationLevel level;
    private UUID parentId;
    private boolean isActive;

    public ProductClassificationResponse(ProductClassification classification) {
        this.id = classification.id();
        this.code = classification.code();
        this.name = classification.name();
        this.level = classification.level();
        this.parentId = classification.parentId();
        this.isActive = classification.isActive();
    }
}