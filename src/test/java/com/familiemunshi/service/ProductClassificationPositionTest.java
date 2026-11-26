package com.familiemunshi.service;

import com.familiemunshi.common.exceptions.BadRequestException;
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

    @InjectMocks
    private ProductClassificationService service;

    private static ProductClassification group(Integer position) {
        return ProductClassification.builder()
                .name("Women")
                .level(ProductClassificationLevel.GROUP)
                .position(position)
                .build();
    }

    @Test
    void create_positionAvailable_savesWithPosition() {
        when(repository.existsByLevelAndPosition(ProductClassificationLevel.GROUP, 1)).thenReturn(false);
        when(repository.existsByCodeIgnoreCase(anyString())).thenReturn(false);
        when(repository.save(any(ProductClassificationDao.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductClassification saved = service.createProductClassification(group(1));

        assertThat(saved.position()).isEqualTo(1);
        verify(repository).existsByLevelAndPosition(ProductClassificationLevel.GROUP, 1);
    }

    @Test
    void create_positionTakenInLevel_throwsConflict() {
        when(repository.existsByLevelAndPosition(ProductClassificationLevel.GROUP, 1)).thenReturn(true);

        assertThatThrownBy(() -> service.createProductClassification(group(1)))
                .isInstanceOf(DuplicateProductClassificationPositionException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void create_nullPosition_throwsBadRequest() {
        assertThatThrownBy(() -> service.createProductClassification(group(null)))
                .isInstanceOf(BadRequestException.class);

        verify(repository, never()).existsByLevelAndPosition(any(), any());
    }

    @Test
    void create_nonPositivePosition_throwsBadRequest() {
        assertThatThrownBy(() -> service.createProductClassification(group(0)))
                .isInstanceOf(BadRequestException.class);

        verify(repository, never()).existsByLevelAndPosition(any(), any());
    }

    @Test
    void update_changingPositionToTakenValue_throwsConflict() {
        UUID id = UUID.randomUUID();
        ProductClassificationDao existing = ProductClassificationDao.builder()
                .id(id)
                .name("Women")
                .code("WOMEN")
                .level(ProductClassificationLevel.GROUP)
                .position(1)
                .isActive(true)
                .build();
        when(repository.findByIdAndIsActiveTrue(id)).thenReturn(java.util.Optional.of(existing));
        when(repository.existsByLevelAndPositionAndIdNot(ProductClassificationLevel.GROUP, 2, id))
                .thenReturn(true);

        ProductClassification patch = ProductClassification.builder().position(2).build();

        assertThatThrownBy(() -> service.updateProductClassification(id, patch))
                .isInstanceOf(DuplicateProductClassificationPositionException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void update_samePosition_skipsUniquenessCheck() {
        UUID id = UUID.randomUUID();
        ProductClassificationDao existing = ProductClassificationDao.builder()
                .id(id)
                .name("Women")
                .code("WOMEN")
                .level(ProductClassificationLevel.GROUP)
                .position(1)
                .isActive(true)
                .build();
        when(repository.findByIdAndIsActiveTrue(id)).thenReturn(java.util.Optional.of(existing));
        when(repository.save(any(ProductClassificationDao.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductClassification patch = ProductClassification.builder().position(1).build();

        service.updateProductClassification(id, patch);

        verify(repository, never()).existsByLevelAndPositionAndIdNot(any(), any(), any());
    }
}
