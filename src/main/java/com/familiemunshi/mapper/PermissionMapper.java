package com.familiemunshi.mapper;

import com.familiemunshi.jpa.daos.PermissionDao;
import com.familiemunshi.service.entities.Permission;
import com.familiemunshi.service.enums.PermissionCode;

public class PermissionMapper {
    private PermissionMapper() {
        throw new IllegalArgumentException("PermissionMapper class cannot be instantiated");
    }
    public static Permission toEntity(PermissionDao permissionDao){
        return Permission.builder()
                .id(permissionDao.getId())
                .code(PermissionCode.fromCode(permissionDao.getName()))
                .description(permissionDao.getDescription())
                .build();
    }
    public static PermissionDao toDao(Permission entity){
        return PermissionDao.builder()
                .id(entity.id())
                .name(entity.code().getCode())
                .description(entity.description())
                .build();
    }
}
