
package com.familiemunshi.service;

import com.familiemunshi.common.exceptions.BadRequestException;
import com.familiemunshi.common.exceptions.ResourceNotFoundException;
import com.familiemunshi.jpa.daos.ProductClassificationDao;
import com.familiemunshi.jpa.repositories.ProductClassificationRepository;
import com.familiemunshi.mapper.ProductClassificationMapper;
import com.familiemunshi.service.entities.ProductClassification;
import com.familiemunshi.service.enums.ProductClassificationLevel;

import com.familiemunshi.service.exceptions.InvalidProductClassificationHierarchyException;
import com.familiemunshi.service.filters.FindAllProductClassificationsFilter;
import com.familiemunshi.common.utils.Util;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductClassificationService {

    private final ProductClassificationRepository productClassificationRepository;

    public ProductClassification createProductClassification(
            ProductClassification classification
    ) {
        validateName(classification.name());
        validateLevel(classification.level());

        ProductClassificationDao parent = resolveParent(
                classification.level(),
                classification.parentId()
        );

        String code = generateUniqueCode(classification.name());

        ProductClassificationDao classificationDao =
                ProductClassificationMapper.toDao(classification);

        classificationDao.setCode(code);
        classificationDao.setIcon(normalizeIcon(classification.icon()));
        classificationDao.setParent(parent);
        classificationDao.setActive(true);

        ProductClassificationDao savedClassification =
                productClassificationRepository.save(classificationDao);

        return ProductClassificationMapper.toEntity(savedClassification);
    }

    @Transactional(readOnly = true)
    public Page<ProductClassification> getAllProductClassificationsByFilter(
            FindAllProductClassificationsFilter filter,
            Pageable pageable
    ) {

        String searchPattern = Util.toSearchPattern(
                filter.getSearch()
        );

        boolean filterLevel = filter.getLevel() != null;

        boolean filterActive = filter.getIsActive() != null;

        boolean activeValue = Boolean.TRUE.equals(
                filter.getIsActive()
        );

        return productClassificationRepository
                .findAllByFilters(
                        searchPattern,
                        filterLevel,
                        filter.getLevel(),
                        filterActive,
                        activeValue,
                        pageable
                )
                .map(ProductClassificationMapper::toEntity);
    }

    @Transactional(readOnly = true)
    public List<ProductClassification> getActiveClassifications() {

        return productClassificationRepository
                .findAllByIsActiveTrueOrderByNameAsc()
                .stream()
                .map(ProductClassificationMapper::toEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductClassification> getRootClassifications() {

        return productClassificationRepository
                .findAllByParentIsNullAndIsActiveTrueOrderByNameAsc()
                .stream()
                .map(ProductClassificationMapper::toEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductClassification> getChildClassifications(
            UUID parentId
    ) {

        getActiveClassification(parentId);

        return productClassificationRepository
                .findAllByParent_IdAndIsActiveTrueOrderByNameAsc(parentId)
                .stream()
                .map(ProductClassificationMapper::toEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductClassification> getClassificationsByLevel(
            ProductClassificationLevel level
    ) {

        validateLevel(level);

        return productClassificationRepository
                .findAllByLevelAndIsActiveTrueOrderByNameAsc(level)
                .stream()
                .map(ProductClassificationMapper::toEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductClassification getProductClassificationById(
            UUID id
    ) {

        return getActiveClassification(id);
    }

    public ProductClassification updateProductClassification(
            UUID id,
            ProductClassification classification
    ) {

        ProductClassificationDao existingClassification =
                findActiveClassificationDao(id);

        String updatedName = classification.name() != null
                ? classification.name().trim()
                : existingClassification.getName();

        ProductClassificationLevel updatedLevel =
                classification.level() != null
                        ? classification.level()
                        : existingClassification.getLevel();

        UUID updatedParentId = classification.parentId() != null
                ? classification.parentId()
                : existingClassification.getParent() != null
                ? existingClassification.getParent().getId()
                : null;

        validateName(updatedName);
        validateLevel(updatedLevel);

        // Validate the new parent and hierarchy.
        ProductClassificationDao updatedParent =
                resolveParent(updatedLevel, updatedParentId);

        // Prevent circular parent relationships.
        validateNoCircularRelationship(id, updatedParent);

        // Validate existing children against the new level.
        validateExistingChildren(id, updatedLevel);

        if (!existingClassification.getName().equals(updatedName)) {

            String updatedCode = generateUniqueCode(updatedName);

            existingClassification.setCode(updatedCode);
        }
        existingClassification.setName(updatedName);
        existingClassification.setLevel(updatedLevel);
        existingClassification.setParent(updatedParent);

        // null = keep current icon, blank = remove icon.
        if (classification.icon() != null) {
            existingClassification.setIcon(normalizeIcon(classification.icon()));
        }

        ProductClassificationDao updatedClassification =
                productClassificationRepository.save(existingClassification);

        return ProductClassificationMapper.toEntity(updatedClassification);
    }


    public void deleteProductClassification(UUID id) {

        ProductClassificationDao classification =
                findActiveClassificationDao(id);

        boolean hasActiveChildren =
                productClassificationRepository
                        .existsByParent_IdAndIsActiveTrue(id);

        if (hasActiveChildren) {
            throw new InvalidProductClassificationHierarchyException(
                    "Cannot delete product classification because it has active child classifications."
            );
        }

        classification.setActive(false);

        productClassificationRepository.save(classification);
    }

    private ProductClassification getActiveClassification(UUID id) {

        return ProductClassificationMapper.toEntity(
                findActiveClassificationDao(id)
        );
    }

    /*
     * Write paths (update, delete, parent assignment) need the managed
     * JPA object so changes are persisted; read paths use the entity.
     */
    private ProductClassificationDao findActiveClassificationDao(UUID id) {

        return productClassificationRepository
                .findByIdAndIsActiveTrue(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product classification",
                                "id",
                                id
                        )
                );
    }

    private ProductClassificationDao resolveParent(
            ProductClassificationLevel level,
            UUID parentId
    ) {

        ProductClassificationLevel requiredParentLevel =
                getRequiredParentLevel(level);

        if (requiredParentLevel == null) {

            if (parentId != null) {
                throw new BadRequestException(
                        "GROUP classification cannot have a parent."
                );
            }

            return null;
        }

        if (parentId == null) {
            throw new BadRequestException(
                    level + " classification requires a parent of level "
                            + requiredParentLevel + "."
            );
        }

        ProductClassificationDao parent =
                findActiveClassificationDao(parentId);

        if (parent.getLevel() != requiredParentLevel) {
            throw new BadRequestException(
                    level + " classification must have a parent with level "
                            + requiredParentLevel + "."
            );
        }

        return parent;
    }

    /*
     * Return the required parent level for a classification level.
     */
    private ProductClassificationLevel getRequiredParentLevel(
            ProductClassificationLevel level
    ) {

        return switch (level) {
            case GROUP -> null;
            case PARENT_CATEGORY -> ProductClassificationLevel.GROUP;
            case CATEGORY -> ProductClassificationLevel.PARENT_CATEGORY;
            case SEGMENT -> ProductClassificationLevel.CATEGORY;
            case SUB_CATEGORY -> ProductClassificationLevel.SEGMENT;
        };
    }

    private ProductClassificationLevel getExpectedChildLevel(
            ProductClassificationLevel level
    ) {

        return switch (level) {
            case GROUP -> ProductClassificationLevel.PARENT_CATEGORY;
            case PARENT_CATEGORY -> ProductClassificationLevel.CATEGORY;
            case CATEGORY -> ProductClassificationLevel.SEGMENT;
            case SEGMENT -> ProductClassificationLevel.SUB_CATEGORY;
            case SUB_CATEGORY -> null;
        };
    }

    private void validateExistingChildren(
            UUID classificationId,
            ProductClassificationLevel newLevel
    ) {

        List<ProductClassificationDao> children =
                productClassificationRepository
                        .findAllByParent_IdAndIsActiveTrueOrderByNameAsc(
                                classificationId
                        );

        if (children.isEmpty()) {
            return;
        }

        ProductClassificationLevel expectedChildLevel =
                getExpectedChildLevel(newLevel);

        if (expectedChildLevel == null) {
            throw new InvalidProductClassificationHierarchyException(
                    "Cannot change classification level because it has active child classifications."
            );
        }

        boolean hasInvalidChild = children.stream()
                .anyMatch(child -> child.getLevel() != expectedChildLevel);

        if (hasInvalidChild) {
            throw new InvalidProductClassificationHierarchyException(
                    "Cannot change classification level because its existing children "
                            + "would no longer follow the classification hierarchy."
            );
        }
    }

    private void validateNoCircularRelationship(
            UUID classificationId,
            ProductClassificationDao newParent
    ) {

        ProductClassificationDao currentParent = newParent;

        while (currentParent != null) {

            if (currentParent.getId().equals(classificationId)) {
                throw new BadRequestException(
                        "Circular product classification relationship is not allowed."
                );
            }

            currentParent = currentParent.getParent();
        }
    }

    private String generateUniqueCode(String name) {

        String baseCode = normalizeCode(name);

        if (baseCode.isBlank()) {
            throw new BadRequestException(
                    "Unable to generate classification code from the provided name."
            );
        }

        String code = baseCode;
        int suffix = 2;

        while (productClassificationRepository.existsByCodeIgnoreCase(code)) {
            code = baseCode + "-" + suffix;
            suffix++;
        }

        return code;
    }

    private String normalizeCode(String name) {

        return Normalizer
                .normalize(name.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }

    private void validateName(String name) {

        if (name == null || name.isBlank()) {
            throw new BadRequestException(
                    "Product classification name is required."
            );
        }

        if (name.trim().length() > 150) {
            throw new BadRequestException(
                    "Product classification name cannot exceed 150 characters."
            );
        }
    }

    private String normalizeIcon(String icon) {

        if (icon == null || icon.isBlank()) {
            return null;
        }

        return icon.trim();
    }

    private void validateLevel(ProductClassificationLevel level) {
        if (level == null) {
            throw new BadRequestException(
                    "Product classification level is required."
            );
        }
    }
}
