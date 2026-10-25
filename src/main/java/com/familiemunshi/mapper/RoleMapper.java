package com.familiemunshi.mapper;

import com.familiemunshi.jpa.daos.RoleDao;
import com.familiemunshi.service.entities.Role;

public class RoleMapper {
    public RoleMapper(){
        throw new IllegalStateException("Utility class should not be instantiated");
    }

    public static Role toEntity(RoleDao roleDao) {
        return Role.builder()
                .id(roleDao.getId())
                .name(roleDao.getName())
                .scope(roleDao.getScope())
                .isSystem(roleDao.getIsSystem())
                .isActive(roleDao.getIsActive())
                .createdAt(roleDao.getCreatedAt())
                .updatedAt(roleDao.getUpdatedAt())
                .build();
    }

    public static RoleDao toDao(Role role){
        return RoleDao.builder()
                .id(role.id())
                .name(role.name())
                .scope(role.scope())
                .isSystem(role.isSystem())
                .isActive(role.isActive())
                .createdAt(role.createdAt())
                .updatedAt(role.updatedAt())
                .build();
    }
}
