
package com.familiemunshi.mapper;

import com.familiemunshi.http.dtos.responses.ProductClassificationResponse;
import com.familiemunshi.http.dtos.responses.ProductClassificationTreeResponse;
import com.familiemunshi.jpa.daos.ProductClassificationDao;
import com.familiemunshi.service.entities.ProductClassification;

import java.util.*;
import java.util.stream.Collectors;

public final class ProductClassificationMapper {

    private ProductClassificationMapper() {
        throw new IllegalArgumentException("ProductClassificationMapper class cannot be instantiated");
    }

    public static ProductClassification toEntity(
            ProductClassificationDao dao
    ) {
        return new ProductClassification(
                dao.getId(),
                dao.getCode(),
                dao.getName(),
                dao.getLevel(),
                dao.getParent() != null
                        ? dao.getParent().getId()
                        : null,
                dao.isActive()
        );
    }

    public static ProductClassificationResponse toResponse(
            ProductClassification entity
    ) {
        return new ProductClassificationResponse(
                entity.id(),
                entity.code(),
                entity.name(),
                entity.level(),
                entity.parentId(),
                entity.isActive()
        );
    }

    public static List<ProductClassificationTreeResponse> toTree(
            List<ProductClassification> classifications
    ) {
        Map<UUID, List<ProductClassificationTreeResponse>> children =
                new HashMap<>();

        Map<UUID, ProductClassificationTreeResponse> nodes =
                classifications.stream().collect(Collectors.toMap(
                        ProductClassification::id,
                        item -> new ProductClassificationTreeResponse(
                                item.id(),
                                item.code(),
                                item.name(),
                                item.level(),
                                new ArrayList<>()
                        )
                ));

        List<ProductClassificationTreeResponse> roots =
                new ArrayList<>();

        for (ProductClassification item : classifications) {

            ProductClassificationTreeResponse node =
                    nodes.get(item.id());

            if (item.parentId() == null) {
                roots.add(node);
                continue;
            }

            children.computeIfAbsent(
                    item.parentId(),
                    ignored -> new ArrayList<>()
            ).add(node);
        }

        for (ProductClassificationTreeResponse node : nodes.values()) {
            List<ProductClassificationTreeResponse> nodeChildren =
                    children.getOrDefault(node.id(), List.of());

            node.children().addAll(nodeChildren);
        }

        return roots;
    }
}
