package com.familiemunshi.mapper;

import com.familiemunshi.jpa.daos.DepartmentDao;
import com.familiemunshi.service.entities.Department;

public class DepartmentMapper {

    private DepartmentMapper() {
        throw new IllegalStateException("Utility class should not be instantiated");
    }

    public static Department toEntity(DepartmentDao dao) {
        if (dao == null) return null;

        return Department.builder()
                .id(dao.getId())
                .name(dao.getName())
                .isActive(dao.isActive())
                .createdAt(dao.getCreatedAt())
                .updatedAt(dao.getUpdatedAt())
                .build();
    }

    public static DepartmentDao toDao(Department domain) {
        if (domain == null) return null;

        return DepartmentDao.builder()
                .id(domain.id())
                .name(domain.name())
                .isActive(domain.isActive())
                .createdAt(domain.createdAt())
                .updatedAt(domain.updatedAt())
                .build();
    }
}
