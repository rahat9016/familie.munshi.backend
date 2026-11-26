package com.familiemunshi.service;

import com.familiemunshi.common.exceptions.BadRequestException;
import com.familiemunshi.common.service.FileStorageService;
import com.familiemunshi.jpa.daos.BrandDao;
import com.familiemunshi.jpa.repositories.BrandRepository;
import com.familiemunshi.service.entities.Brand;
import com.familiemunshi.service.exceptions.BrandTitleAlreadyExistsException;
import com.familiemunshi.service.exceptions.DuplicateBrandPositionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BrandServiceTest {

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private BrandService brandService;

    private static Brand brand(Integer position) {
        return Brand.builder().title("Nike").description("Sportswear").position(position).build();
    }

    private static MockMultipartFile logo() {
        byte[] pngHeader = {
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
                0x00, 0x00, 0x00, 0x0D
        };
        return new MockMultipartFile("logo", "logo.png", "image/png", pngHeader);
    }

    @Test
    void createBrand_titleTaken_throwsConflict() {
        when(brandRepository.existsByTitleIgnoreCase("Nike")).thenReturn(true);

        assertThatThrownBy(() -> brandService.createBrand(brand(1), logo()))
                .isInstanceOf(BrandTitleAlreadyExistsException.class);

        verify(brandRepository, never()).saveAndFlush(any());
    }

    @Test
    void createBrand_positionTaken_throwsConflict() {
        when(brandRepository.existsByTitleIgnoreCase("Nike")).thenReturn(false);
        when(brandRepository.existsByPosition(1)).thenReturn(true);

        assertThatThrownBy(() -> brandService.createBrand(brand(1), logo()))
                .isInstanceOf(DuplicateBrandPositionException.class);

        verify(brandRepository, never()).saveAndFlush(any());
    }

    @Test
    void createBrand_nullPosition_throwsBadRequest() {
        assertThatThrownBy(() -> brandService.createBrand(brand(null), logo()))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void createBrand_missingLogo_throwsBadRequest() {
        when(brandRepository.existsByTitleIgnoreCase("Nike")).thenReturn(false);
        when(brandRepository.existsByPosition(1)).thenReturn(false);

        assertThatThrownBy(() -> brandService.createBrand(brand(1), null))
                .isInstanceOf(BadRequestException.class);

        verify(brandRepository, never()).saveAndFlush(any());
    }

    @Test
    void updateBrand_positionTakenByAnother_throwsConflict() {
        java.util.UUID id = java.util.UUID.randomUUID();
        BrandDao existing = BrandDao.builder()
                .id(id).title("Nike").logoUrl("brands/logos/n.png").position(1).isActive(true).build();
        when(brandRepository.findById(id)).thenReturn(java.util.Optional.of(existing));
        when(brandRepository.existsByPositionAndIdNot(2, id)).thenReturn(true);

        com.familiemunshi.http.dtos.requests.UpdateBrandRequest request =
                new com.familiemunshi.http.dtos.requests.UpdateBrandRequest();
        request.setPosition(2);

        assertThatThrownBy(() -> brandService.updateBrand(id, request))
                .isInstanceOf(DuplicateBrandPositionException.class);

        verify(brandRepository, never()).saveAndFlush(any());
    }
}
