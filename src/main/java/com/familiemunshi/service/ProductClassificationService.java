package com.familiemunshi.service;

import com.familiemunshi.common.component.StorageFileSupport;
import com.familiemunshi.common.exceptions.BadRequestException;
import com.familiemunshi.common.exceptions.ResourceNotFoundException;
import com.familiemunshi.common.service.FileStorageService;
import com.familiemunshi.common.utils.Util;
import com.familiemunshi.http.validations.ValidationUtil;
import com.familiemunshi.jpa.daos.ProductClassificationDao;
import com.familiemunshi.jpa.repositories.ProductClassificationRepository;
import com.familiemunshi.mapper.ProductClassificationMapper;
import com.familiemunshi.service.entities.ProductClassification;
import com.familiemunshi.service.enums.ProductClassificationLevel;
import com.familiemunshi.service.exceptions.DuplicateProductClassificationPositionException;
import com.familiemunshi.service.exceptions.InvalidProductClassificationHierarchyException;
import com.familiemunshi.service.filters.FindAllProductClassificationsFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.text.Normalizer;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductClassificationService {

    private static final String ICON_DIRECTORY =
            StorageFileSupport.PRODUCT_CLASSIFICATION_ICONS;
    private static final long ICON_MAX_SIZE_KB = 512;
    private static final int ICON_URL_EXPIRY_MINUTES = 7 * 24 * 60;
    private static final int DESCRIPTION_MAX_LENGTH = 1000;

    private final ProductClassificationRepository productClassificationRepository;
    private final FileStorageService fileStorageService;

    public ProductClassification createProductClassification(
            ProductClassification classification,
            MultipartFile iconFile
    ) {
        validateIconInput(classification.icon(), iconFile);
        validateName(classification.name());
        validateDescription(classification.description());
        validateLevel(classification.level());
        validatePosition(classification.position());
        ensurePositionAvailable(
                classification.level(),
                classification.position(),
                null
        );

        ProductClassificationDao parent = resolveParent(
                classification.level(),
                classification.parentId()
        );

        String code = generateUniqueCode(classification.name());

        ProductClassificationDao classificationDao =
                ProductClassificationMapper.toDao(classification);

        classificationDao.setCode(code);
        classificationDao.setDescription(classification.description().trim());
        classificationDao.setParent(parent);
        classificationDao.setActive(classification.isActive());

        // Upload last, after every validation has passed.
        classificationDao.setIcon(resolveNewIcon(classification.icon(), iconFile));

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

    /*
     * The group and every active classification under it.
     * Other groups are not loaded.
     */
    @Transactional(readOnly = true)
    public List<ProductClassification> getActiveClassificationsByGroup(
            UUID groupId
    ) {

        ProductClassification group = getActiveClassification(groupId);

        if (group.level() != ProductClassificationLevel.GROUP) {
            throw new BadRequestException(
                    "Product classification " + groupId + " is not a GROUP."
            );
        }

        return productClassificationRepository
                .findActiveSubtree(groupId)
                .stream()
                .map(ProductClassificationMapper::toEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductClassification> getRootClassifications() {

        return productClassificationRepository
                .findAllByParentIsNullAndIsActiveTrueOrderByPositionAsc()
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
                .findAllByParent_IdAndIsActiveTrueOrderByPositionAsc(parentId)
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
                .findAllByLevelAndIsActiveTrueOrderByPositionAsc(level)
                .stream()
                .map(ProductClassificationMapper::toEntity)
                .toList();
    }

    /*
     * Admin view (PRODUCT_CLASSIFICATION_VIEW): returns active and
     * inactive classifications so inactive ones can be edited.
     */
    @Transactional(readOnly = true)
    public ProductClassification getProductClassificationById(
            UUID id
    ) {

        return ProductClassificationMapper.toEntity(
                findClassificationDao(id)
        );
    }

    /*
     * Icon rules (single icon column):
     *   iconFile sent     -> upload to S3, store key
     *   icon = "shirt"    -> store lucide name
     *   icon = ""         -> remove icon
     *   neither sent      -> keep current icon
     *   both sent         -> 400
     * A replaced or removed uploaded file is deleted after commit.
     */
    public ProductClassification updateProductClassification(
            UUID id,
            ProductClassification classification,
            MultipartFile iconFile,
            Boolean isActive
    ) {

        validateIconInput(classification.icon(), iconFile);

        // Any status, so a deactivated classification can be re-activated.
        ProductClassificationDao existingClassification =
                findClassificationDao(id);

        String updatedName = classification.name() != null
                ? classification.name().trim()
                : existingClassification.getName();

        // null = keep current; blank is rejected by validateDescription.
        String updatedDescription = classification.description() != null
                ? classification.description().trim()
                : existingClassification.getDescription();

        ProductClassificationLevel updatedLevel =
                classification.level() != null
                        ? classification.level()
                        : existingClassification.getLevel();

        UUID updatedParentId = classification.parentId() != null
                ? classification.parentId()
                : existingClassification.getParent() != null
                ? existingClassification.getParent().getId()
                : null;

        Integer updatedPosition = classification.position() != null
                ? classification.position()
                : existingClassification.getPosition();

        validateName(updatedName);
        validateDescription(updatedDescription);
        validateLevel(updatedLevel);
        validatePosition(updatedPosition);

        // Validate the new parent and hierarchy.
        ProductClassificationDao updatedParent =
                resolveParent(updatedLevel, updatedParentId);

        // Prevent circular parent relationships.
        validateNoCircularRelationship(id, updatedParent);

        // Validate existing children against the new level.
        validateExistingChildren(id, updatedLevel);

        if (!updatedPosition.equals(existingClassification.getPosition())
                || updatedLevel != existingClassification.getLevel()) {
            ensurePositionAvailable(updatedLevel, updatedPosition, id);
        }

        if (!existingClassification.getName().equals(updatedName)) {

            String updatedCode = generateUniqueCode(updatedName);

            existingClassification.setCode(updatedCode);
        }
        existingClassification.setName(updatedName);
        existingClassification.setDescription(updatedDescription);
        existingClassification.setLevel(updatedLevel);
        existingClassification.setParent(updatedParent);
        existingClassification.setPosition(updatedPosition);

        if (isActive != null) {
            existingClassification.setActive(isActive);
        }

        String previousIcon = existingClassification.getIcon();

        if (hasFile(iconFile) || classification.icon() != null) {
            existingClassification.setIcon(
                    resolveNewIcon(classification.icon(), iconFile)
            );
        }

        ProductClassificationDao updatedClassification =
                productClassificationRepository.save(existingClassification);

        if (ProductClassification.isUploadedIcon(previousIcon)
                && !previousIcon.equals(updatedClassification.getIcon())) {
            deleteFileAfterCommit(previousIcon);
        }

        return ProductClassificationMapper.toEntity(updatedClassification);
    }


    public void deleteProductClassification(UUID id) {

        ProductClassificationDao classification = findClassificationDao(id);

        if (productClassificationRepository.existsByParent_Id(id)) {
            throw new InvalidProductClassificationHierarchyException(
                    "Cannot delete product classification because it has child classifications. "
                            + "Delete its children first."
            );
        }

        String icon = classification.getIcon();

        productClassificationRepository.delete(classification);

        if (ProductClassification.isUploadedIcon(icon)) {
            deleteFileAfterCommit(icon);
        }
    }

    /** Presigned URL for an uploaded icon, null for lucide icons or no icon. */
    public String getIconUrl(ProductClassification classification) {

        if (classification == null || !classification.hasUploadedIcon()) {
            return null;
        }

        return fileStorageService.getPresignedUrl(
                classification.icon(),
                ICON_URL_EXPIRY_MINUTES
        );
    }

    // ------------------------------------------------------------------
    // Icon handling
    // ------------------------------------------------------------------

    /*
     * Validate before any DB work or upload: one source only,
     * file format/size/content, lucide name format.
     */
    private void validateIconInput(String lucideName, MultipartFile iconFile) {

        boolean hasName = lucideName != null && !lucideName.isBlank();

        if (hasFile(iconFile) && hasName) {
            throw new BadRequestException("iconSourceConflict");
        }

        if (hasFile(iconFile)) {
            ValidationUtil.validateIcon(iconFile, ICON_MAX_SIZE_KB);
        } else {
            ValidationUtil.validateLucideIconName(lucideName);
        }
    }

    /*
     * Value for the icon column: S3 key for a file, trimmed lucide
     * name, or null. Input must already be validated.
     */
    private String resolveNewIcon(String lucideName, MultipartFile iconFile) {

        if (hasFile(iconFile)) {
            return uploadIcon(iconFile);
        }

        if (lucideName == null || lucideName.isBlank()) {
            return null;
        }

        return lucideName.trim();
    }

    private String uploadIcon(MultipartFile iconFile) {

        requireActiveTransaction();

        String key = fileStorageService.uploadPublicFile(iconFile, ICON_DIRECTORY);

        deleteFileOnRollback(key);

        return key;
    }

    private static boolean hasFile(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    private void requireActiveTransaction() {
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || !TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("Active transaction required");
        }
    }

    private void deleteFileOnRollback(String key) {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        if (status != STATUS_COMMITTED) {
                            safeDeleteFile(key);
                        }
                    }
                }
        );
    }

    private void deleteFileAfterCommit(String key) {

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            safeDeleteFile(key);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        safeDeleteFile(key);
                    }
                }
        );
    }

    private void safeDeleteFile(String key) {
        try {
            fileStorageService.deleteFile(key);
        } catch (Exception exception) {
            log.error("Failed to delete product classification icon: {}", key, exception);
        }
    }

    // ------------------------------------------------------------------
    // Lookup
    // ------------------------------------------------------------------

    private ProductClassification getActiveClassification(UUID id) {

        return ProductClassificationMapper.toEntity(
                findActiveClassificationDao(id)
        );
    }

    private ProductClassificationDao findClassificationDao(UUID id) {

        return productClassificationRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product classification",
                                "id",
                                id
                        )
                );
    }

    /*
     * Write paths (parent assignment) need the managed JPA object so
     * changes are persisted; read paths use the entity.
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

    // ------------------------------------------------------------------
    // Hierarchy
    // ------------------------------------------------------------------

    private ProductClassificationDao resolveParent(
            ProductClassificationLevel level,
            UUID parentId
    ) {

        Set<ProductClassificationLevel> allowedParentLevels =
                getAllowedParentLevels(level);

        if (allowedParentLevels.isEmpty()) {

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
                            + describeLevels(allowedParentLevels) + "."
            );
        }

        ProductClassificationDao parent =
                findActiveClassificationDao(parentId);

        if (!allowedParentLevels.contains(parent.getLevel())) {
            throw new BadRequestException(
                    level + " classification must have a parent with level "
                            + describeLevels(allowedParentLevels) + "."
            );
        }

        return parent;
    }

    /*
     * Allowed parent levels for a classification level.
     * GROUP -> PARENT_CATEGORY -> [SEGMENT] -> CATEGORY -> SUB_CATEGORY
     * SEGMENT is optional: CATEGORY may sit directly under PARENT_CATEGORY.
     */
    private Set<ProductClassificationLevel> getAllowedParentLevels(
            ProductClassificationLevel level
    ) {

        return switch (level) {
            case GROUP -> EnumSet.noneOf(ProductClassificationLevel.class);
            case PARENT_CATEGORY -> EnumSet.of(ProductClassificationLevel.GROUP);
            case SEGMENT -> EnumSet.of(ProductClassificationLevel.PARENT_CATEGORY);
            case CATEGORY -> EnumSet.of(
                    ProductClassificationLevel.PARENT_CATEGORY,
                    ProductClassificationLevel.SEGMENT
            );
            case SUB_CATEGORY -> EnumSet.of(ProductClassificationLevel.CATEGORY);
        };
    }

    private Set<ProductClassificationLevel> getAllowedChildLevels(
            ProductClassificationLevel level
    ) {

        return switch (level) {
            case GROUP -> EnumSet.of(ProductClassificationLevel.PARENT_CATEGORY);
            case PARENT_CATEGORY -> EnumSet.of(
                    ProductClassificationLevel.SEGMENT,
                    ProductClassificationLevel.CATEGORY
            );
            case SEGMENT -> EnumSet.of(ProductClassificationLevel.CATEGORY);
            case CATEGORY -> EnumSet.of(ProductClassificationLevel.SUB_CATEGORY);
            case SUB_CATEGORY -> EnumSet.noneOf(ProductClassificationLevel.class);
        };
    }

    private String describeLevels(Set<ProductClassificationLevel> levels) {

        return levels.stream()
                .map(Enum::name)
                .collect(Collectors.joining(" or "));
    }

    private void validateExistingChildren(
            UUID classificationId,
            ProductClassificationLevel newLevel
    ) {

        List<ProductClassificationDao> children =
                productClassificationRepository
                        .findAllByParent_IdAndIsActiveTrueOrderByPositionAsc(
                                classificationId
                        );

        if (children.isEmpty()) {
            return;
        }

        Set<ProductClassificationLevel> allowedChildLevels =
                getAllowedChildLevels(newLevel);

        if (allowedChildLevels.isEmpty()) {
            throw new InvalidProductClassificationHierarchyException(
                    "Cannot change classification level because it has active child classifications."
            );
        }

        boolean hasInvalidChild = children.stream()
                .anyMatch(child -> !allowedChildLevels.contains(child.getLevel()));

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

    // ------------------------------------------------------------------
    // Field validation
    // ------------------------------------------------------------------

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

    private void validateDescription(String description) {

        if (description == null || description.isBlank()) {
            throw new BadRequestException(
                    "Product classification description is required."
            );
        }

        if (description.trim().length() > DESCRIPTION_MAX_LENGTH) {
            throw new BadRequestException(
                    "Product classification description cannot exceed "
                            + DESCRIPTION_MAX_LENGTH + " characters."
            );
        }
    }

    private void validateLevel(ProductClassificationLevel level) {
        if (level == null) {
            throw new BadRequestException(
                    "Product classification level is required."
            );
        }
    }

    private void validatePosition(Integer position) {
        if (position == null) {
            throw new BadRequestException(
                    "Product classification position is required."
            );
        }

        if (position <= 0) {
            throw new BadRequestException(
                    "Product classification position must be a positive number."
            );
        }
    }

    private void ensurePositionAvailable(
            ProductClassificationLevel level,
            Integer position,
            UUID excludeId
    ) {
        boolean taken = excludeId == null
                ? productClassificationRepository
                        .existsByLevelAndPosition(level, position)
                : productClassificationRepository
                        .existsByLevelAndPositionAndIdNot(level, position, excludeId);

        if (taken) {
            throw new DuplicateProductClassificationPositionException(position, level);
        }
    }
}
