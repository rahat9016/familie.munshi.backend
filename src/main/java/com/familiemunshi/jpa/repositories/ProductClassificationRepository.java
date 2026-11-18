
package com.familiemunshi.jpa.repositories;

import com.familiemunshi.jpa.daos.ProductClassificationDao;
import com.familiemunshi.service.enums.ProductClassificationLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductClassificationRepository
        extends JpaRepository<ProductClassificationDao, UUID> {

    Optional<ProductClassificationDao> findByIdAndIsActiveTrue(
            UUID id
    );

    Page<ProductClassificationDao> findAllByIsActiveTrue(
            Pageable pageable
    );

    List<ProductClassificationDao> findAllByIsActiveTrueOrderByNameAsc();

    List<ProductClassificationDao>
    findAllByParent_IdAndIsActiveTrueOrderByNameAsc(UUID parentId);

    List<ProductClassificationDao>
    findAllByParentIsNullAndIsActiveTrueOrderByNameAsc();

    List<ProductClassificationDao>
    findAllByLevelAndIsActiveTrueOrderByNameAsc(
            ProductClassificationLevel level
    );

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(
            String code,
            UUID id
    );

    boolean existsByParent_IdAndIsActiveTrue(UUID parentId);
}
