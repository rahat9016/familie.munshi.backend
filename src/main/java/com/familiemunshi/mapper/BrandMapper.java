package com.familiemunshi.mapper;

import com.familiemunshi.jpa.daos.BrandDao;
import com.familiemunshi.service.entities.Brand;

public class BrandMapper {

    private BrandMapper() {
        throw new IllegalStateException("Utility class should not be instantiated");
    }

    public static Brand toEntity(BrandDao dao) {
        if (dao == null) return null;

        return Brand.builder()
                .id(dao.getId())
                .title(dao.getTitle())
                .description(dao.getDescription())
                .logoUrl(dao.getLogoUrl())
                .position(dao.getPosition())
                .isActive(dao.isActive())
                .createdAt(dao.getCreatedAt())
                .updatedAt(dao.getUpdatedAt())
                .build();
    }

    public static BrandDao toDao(Brand domain) {
        if (domain == null) return null;

        return BrandDao.builder()
                .id(domain.id())
                .title(domain.title())
                .description(domain.description())
                .logoUrl(domain.logoUrl())
                .position(domain.position())
                .isActive(domain.isActive())
                .createdAt(domain.createdAt())
                .updatedAt(domain.updatedAt())
                .build();
    }
}
