package com.familiemunshi.jpa.repositories;

import com.familiemunshi.jpa.daos.ProductClassificationDao;
import com.familiemunshi.service.enums.ProductClassificationLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductClassificationRepository extends JpaRepository<ProductClassificationDao, UUID> {
    List<ProductClassificationDao> findByParentIsNull();

    List<ProductClassificationDao> findByParentId(UUID parentId);

    List<ProductClassificationDao> findByLevel(
            ProductClassificationLevel level
    );

    List<ProductClassificationDao> findByParentIdAndLevel(
            UUID parentId,
            ProductClassificationLevel level
    );

    boolean existsByParentIdAndCode(UUID parentId, String code);

    boolean existsByParentId(UUID parentId);
}
