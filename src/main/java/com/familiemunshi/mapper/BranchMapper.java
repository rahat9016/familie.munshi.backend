package com.familiemunshi.mapper;

import com.familiemunshi.jpa.daos.BranchDao;
import com.familiemunshi.service.entities.Branch;

public class BranchMapper {
    private BranchMapper(){
        throw new IllegalStateException("Utility class should not be instantiated");
    }

    public static Branch toEntity(BranchDao dao) {
        if(dao == null) return null;
        return Branch.builder()
                .id(dao.getId())
                .code(dao.getCode())
                .name(dao.getName())
                .address(dao.getAddress())
                .phone(dao.getPhone())
                .logoUrl(dao.getLogoUrl())
                .isActive(dao.isActive())
                .createdAt(dao.getCreatedAt())
                .updatedAt(dao.getUpdatedAt())
                .build();
    }

    public static BranchDao toDao(Branch domain){
        if(domain == null) return null;
        return BranchDao.builder()
                .id(domain.id())
                .code(domain.code())
                .name(domain.name())
                .address(domain.address())
                .phone(domain.phone())
                .logoUrl(domain.logoUrl())
                .isActive(domain.isActive())
                .createdAt(domain.createdAt())
                .updatedAt(domain.updatedAt())
                .build();
    }
}
