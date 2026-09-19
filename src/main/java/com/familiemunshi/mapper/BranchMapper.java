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
                .name(dao.getName())
                .address(dao.getAddress())
                .isActive(dao.isActive())
                .build();
    }

    public static BranchDao toDao(Branch domain){
        if(domain == null) return null;
        return BranchDao.builder()
                .id(domain.id())
                .name(domain.name())
                .address(domain.address())
                .isActive(domain.isActive())
                .build();
    }
}
