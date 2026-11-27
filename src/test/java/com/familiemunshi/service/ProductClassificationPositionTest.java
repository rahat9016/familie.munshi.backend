package com.familiemunshi.service;

import com.familiemunshi.common.exceptions.BadRequestException;
import com.familiemunshi.common.service.FileStorageService;
import com.familiemunshi.jpa.daos.ProductClassificationDao;
import com.familiemunshi.jpa.repositories.ProductClassificationRepository;
import com.familiemunshi.service.entities.ProductClassification;
import com.familiemunshi.service.enums.ProductClassificationLevel;
import com.familiemunshi.service.exceptions.DuplicateProductClassificationPositionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductClassificationPositionTest {

    @Mock
    private ProductClassificationRepository repository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private ProductClassificationService service;

    private static ProductClassification group(Integer position) {
        return ProductClassification.builder()
                .name("Women")
                .level(ProductClassificationLevel.GROUP)
                .position(position)
                .isActive(true)
                .build();
    }

    private static ProductClassificationDao existingGroup(UUID id) {
        return ProductClassificationDao.builder()
                .id(id)
                .name("Women")
                .code("WOMEN")
                .level(ProductClassificationLevel.GROUP)
                .position(1)
                .isActive(true)
                .build();
    }

    @Test
    void create_positionAvailable_savesWithPosition() {
        when(repository.existsByLevelAndPosition(ProductClassificationLevel.GROUP, 1)).thenReturn(false);
        when(repository.existsByCodeIgnoreCase(anyString())).thenReturn(false);
        when(repository.save(any(ProductClassificationDao.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductClassification saved = service.createProductClassification(group(1), null);

        assertThat(saved.position()).isEqualTo(1);
        verify(repository).existsByLevelAndPosition(ProductClassificationLevel.GROUP, 1);
    }

    @Test
    void create_lucideIcon_storesNameAsIs() {
        when(repository.existsByLevelAndPosition(ProductClassificationLevel.GROUP, 1)).thenReturn(false);
        when(repository.existsByCodeIgnoreCase(anyString())).thenReturn(false);
        when(repository.save(any(ProductClassificationDao.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductClassification saved = service.createProductClassification(
                group(1).withIcon("shopping-cart"), null);

        assertThat(saved.icon()).isEqualTo("shopping-cart");
        assertThat(saved.hasUploadedIcon()).isFalse();
    }

    @Test
    void create_invalidLucideIcon_throwsBadRequest() {
        assertThatThrownBy(() -> service.createProductClassification(
                group(1).withIcon("public/evil.svg"), null))
                .isInstanceOf(BadRequestException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void create_inactive_savesInactive() {
        when(repository.existsByLevelAndPosition(ProductClassificationLevel.GROUP, 1)).thenReturn(false);
        when(repository.existsByCodeIgnoreCase(anyString())).thenReturn(false);
        when(repository.save(any(ProductClassificationDao.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductClassification saved = service.createProductClassification(
                group(1).withActive(false), null);

        assertThat(saved.isActive()).isFalse();
    }

    @Test
    void create_positionTakenInLevel_throwsConflict() {
        when(repository.existsByLevelAndPosition(ProductClassificationLevel.GROUP, 1)).thenReturn(true);

        assertThatThrownBy(() -> service.createProductClassification(group(1), null))
                .isInstanceOf(DuplicateProductClassificationPositionException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void create_nullPosition_throwsBadRequest() {
        assertThatThrownBy(() -> service.createProductClassification(group(null), null))
                .isInstanceOf(BadRequestException.class);

        verify(repository, never()).existsByLevelAndPosition(any(), any());
    }

    @Test
    void create_nonPositivePosition_throwsBadRequest() {
        assertThatThrownBy(() -> service.createProductClassification(group(0), null))
                .isInstanceOf(BadRequestException.class);

        verify(repository, never()).existsByLevelAndPosition(any(), any());
    }

    @Test
    void update_changingPositionToTakenValue_throwsConflict() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(existingGroup(id)));
        when(repository.existsByLevelAndPositionAndIdNot(ProductClassificationLevel.GROUP, 2, id))
                .thenReturn(true);

        ProductClassification patch = ProductClassification.builder().position(2).build();

        assertThatThrownBy(() -> service.updateProductClassification(id, patch, null, null))
                .isInstanceOf(DuplicateProductClassificationPositionException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void update_samePosition_skipsUniquenessCheck() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(existingGroup(id)));
        when(repository.save(any(ProductClassificationDao.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductClassification patch = ProductClassification.builder().position(1).build();

        service.updateProductClassification(id, patch, null, null);

        verify(repository, never()).existsByLevelAndPositionAndIdNot(any(), any(), any());
    }

    @Test
    void update_inactiveClassification_canBeReactivated() {
        UUID id = UUID.randomUUID();
        ProductClassificationDao existing = existingGroup(id);
        existing.setActive(false);
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(ProductClassificationDao.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductClassification updated = service.updateProductClassification(
                id, ProductClassification.builder().build(), null, true);

        assertThat(updated.isActive()).isTrue();
    }

    @Test
    void update_uploadedIconReplacedByLucide_deletesOldFile() {
        UUID id = UUID.randomUUID();
        ProductClassificationDao existing = existingGroup(id);
        existing.setIcon("public/product-classifications/icons/old.svg");
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(ProductClassificationDao.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductClassification updated = service.updateProductClassification(
                id, ProductClassification.builder().icon("shirt").build(), null, null);

        assertThat(updated.icon()).isEqualTo("shirt");
        // no transaction in a unit test -> deleted immediately
        verify(fileStorageService).deleteFile("public/product-classifications/icons/old.svg");
    }
}
