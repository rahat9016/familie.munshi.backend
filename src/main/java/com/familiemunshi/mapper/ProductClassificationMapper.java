package com.familiemunshi.mapper;

import com.familiemunshi.http.dtos.responses.ProductClassificationResponse;
import com.familiemunshi.http.dtos.responses.ProductClassificationTreeResponse;
import com.familiemunshi.jpa.daos.ProductClassificationDao;
import com.familiemunshi.service.entities.ProductClassification;

import java.util.*;

public class ProductClassificationMapper {

    private ProductClassificationMapper() {
        throw new IllegalStateException("Utility class should not be instantiated");
    }

    public static ProductClassification toEntity(ProductClassificationDao dao) {
        if (dao == null) return null;

        return ProductClassification.builder()
                .id(dao.getId())
                .code(dao.getCode())
                .name(dao.getName())
                .level(dao.getLevel())
                .parentId(dao.getParent() != null
                        ? dao.getParent().getId()
                        : null)
                .isActive(dao.isActive())
                .build();
    }

    public static ProductClassificationDao toDao(ProductClassification domain) {
        if (domain == null) return null;

        return ProductClassificationDao.builder()
                .id(domain.id())
                .code(domain.code())
                .name(domain.name())
                .level(domain.level())
                .parent(domain.parentId() != null
                        ? ProductClassificationDao.builder()
                        .id(domain.parentId())
                        .build()
                        : null)
                .isActive(domain.isActive())
                .build();
    }

    public static List<ProductClassificationTreeResponse> toTree(
            List<ProductClassification> classifications
    ) {
        if (classifications == null || classifications.isEmpty()) {
            return Collections.emptyList();
        }

        Map<UUID, ProductClassificationTreeResponse> nodes = new HashMap<>();

        for (ProductClassification item : classifications) {
            nodes.put(
                    item.id(),
                    new ProductClassificationTreeResponse(
                            item.id(),
                            item.code(),
                            item.name(),
                            item.level(),
                            new ArrayList<>()
                    )
            );
        }

        List<ProductClassificationTreeResponse> roots = new ArrayList<>();

        for (ProductClassification item : classifications) {
            ProductClassificationTreeResponse node = nodes.get(item.id());

            if (item.parentId() == null) {
                roots.add(node);
                continue;
            }

            ProductClassificationTreeResponse parent = nodes.get(item.parentId());

            if (parent != null) {
                parent.children().add(node);
            }
        }

        return roots;
    }
}