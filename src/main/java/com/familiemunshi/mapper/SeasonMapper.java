package com.familiemunshi.mapper;

import com.familiemunshi.jpa.daos.SeasonDao;
import com.familiemunshi.service.entities.Season;

public class SeasonMapper {

    private SeasonMapper() {
        throw new IllegalStateException("Utility class should not be instantiated");
    }

    public static Season toEntity(SeasonDao dao) {
        if (dao == null) return null;

        return Season.builder()
                .id(dao.getId())
                .season(dao.getSeason())
                .status(dao.getStatus())
                .createdAt(dao.getCreatedAt())
                .updatedAt(dao.getUpdatedAt())
                .build();
    }

    public static SeasonDao toDao(Season domain) {
        if (domain == null) return null;

        return SeasonDao.builder()
                .id(domain.id())
                .season(domain.season())
                .status(domain.status())
                .createdAt(domain.createdAt())
                .updatedAt(domain.updatedAt())
                .build();
    }
}
