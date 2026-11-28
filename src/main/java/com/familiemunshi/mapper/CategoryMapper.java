package com.familiemunshi.mapper;

import com.familiemunshi.jpa.daos.CategoryDao;
import com.familiemunshi.service.entities.Category;

public class CategoryMapper {

    private CategoryMapper() {
        throw new IllegalStateException("Utility class should not be instantiated");
    }

    public static Category toEntity(CategoryDao dao) {
        if (dao == null) return null;

        return Category.builder()
                .id(dao.getId())
                .departmentId(dao.getDepartmentId())
                .name(dao.getName())
                .isActive(dao.isActive())
                .createdAt(dao.getCreatedAt())
                .updatedAt(dao.getUpdatedAt())
                .build();
    }

    public static CategoryDao toDao(Category domain) {
        if (domain == null) return null;

        return CategoryDao.builder()
                .id(domain.id())
                .departmentId(domain.departmentId())
                .name(domain.name())
                .isActive(domain.isActive())
                .createdAt(domain.createdAt())
                .updatedAt(domain.updatedAt())
                .build();
    }
}
