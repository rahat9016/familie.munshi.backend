package com.familiemunshi.mapper;

import com.familiemunshi.jpa.daos.ColorwayDao;
import com.familiemunshi.jpa.daos.ColorwayImageDao;
import com.familiemunshi.service.entities.Colorway;
import com.familiemunshi.service.entities.ColorwayImage;

public class ColorwayMapper {

    private ColorwayMapper() {
        throw new IllegalStateException("Utility class should not be instantiated");
    }

    public static Colorway toEntity(ColorwayDao dao) {
        if (dao == null) return null;

        return Colorway.builder()
                .id(dao.getId())
                .code(dao.getCode())
                .marketingName(dao.getMarketingName())
                .name(dao.getName())
                .colorSpecification(dao.getColorSpecification())
                .description(dao.getDescription())
                .colorStandard(dao.getColorStandard())
                .colorReference(dao.getColorReference())
                .hexCode(dao.getHexCode())
                .isActive(dao.isActive())
                .inTheme(dao.isInTheme())
                .sustainabilityLabel(dao.isSustainabilityLabel())
                .planSms(dao.isPlanSms())
                .plan3dSms(dao.isPlan3dSms())
                .actualSms(dao.isActualSms())
                .startDate(dao.getStartDate())
                .endDate(dao.getEndDate())
                .clearanceDate(dao.getClearanceDate())
                .images(dao.getImages().stream().map(ColorwayMapper::toImageEntity).toList())
                .createdAt(dao.getCreatedAt())
                .updatedAt(dao.getUpdatedAt())
                .build();
    }

    /** Images not mapped; service attaches via ColorwayDao#addImage. */
    public static ColorwayDao toDao(Colorway domain) {
        if (domain == null) return null;

        return ColorwayDao.builder()
                .id(domain.id())
                .code(domain.code())
                .marketingName(domain.marketingName())
                .name(domain.name())
                .colorSpecification(domain.colorSpecification())
                .description(domain.description())
                .colorStandard(domain.colorStandard())
                .colorReference(domain.colorReference())
                .hexCode(domain.hexCode())
                .isActive(domain.isActive())
                .inTheme(domain.inTheme())
                .sustainabilityLabel(domain.sustainabilityLabel())
                .planSms(domain.planSms())
                .plan3dSms(domain.plan3dSms())
                .actualSms(domain.actualSms())
                .startDate(domain.startDate())
                .endDate(domain.endDate())
                .clearanceDate(domain.clearanceDate())
                .createdAt(domain.createdAt())
                .updatedAt(domain.updatedAt())
                .build();
    }

    public static ColorwayImage toImageEntity(ColorwayImageDao dao) {
        return ColorwayImage.builder()
                .id(dao.getId())
                .imageKey(dao.getImageKey())
                .position(dao.getPosition())
                .build();
    }
}
