package com.familiemunshi.service;

import com.familiemunshi.common.component.StorageFileSupport;
import com.familiemunshi.common.exceptions.BadRequestException;
import com.familiemunshi.common.service.FileStorageService;
import com.familiemunshi.common.utils.Util;
import com.familiemunshi.http.dtos.requests.UpdateBrandRequest;
import com.familiemunshi.http.validations.ValidationUtil;
import com.familiemunshi.jpa.daos.BrandDao;
import com.familiemunshi.jpa.repositories.BrandRepository;
import com.familiemunshi.mapper.BrandMapper;
import com.familiemunshi.service.entities.Brand;
import com.familiemunshi.service.exceptions.BrandNotFoundException;
import com.familiemunshi.service.exceptions.BrandTitleAlreadyExistsException;
import com.familiemunshi.service.exceptions.DuplicateBrandPositionException;
import com.familiemunshi.service.filters.FindAllBrandsFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BrandService {

    private static final int LOGO_URL_EXPIRY_MINUTES = 7 * 24 * 60;
    private static final String LOGO_DIRECTORY = StorageFileSupport.BRAND_LOGOS;

    private final BrandRepository brandRepository;
    private final FileStorageService fileStorageService;

    public Brand createBrand(Brand brand, MultipartFile logoFile) {

        validatePosition(brand.position());

        if (brandRepository.existsByTitleIgnoreCase(brand.title())) {
            throw new BrandTitleAlreadyExistsException(brand.title());
        }
        if (brandRepository.existsByPosition(brand.position())) {
            throw new DuplicateBrandPositionException(brand.position());
        }

        if (logoFile == null || logoFile.isEmpty()) {
            throw new BadRequestException("Brand logo is required.");
        }
        ValidationUtil.validateMediaType(logoFile);
        requireActiveTransaction();

        String filename = fileStorageService.uploadPublicFile(logoFile, LOGO_DIRECTORY);
        registerUploadedFileCleanup(filename);

        brand = brand.withLogoUrl(filename);

        BrandDao saved = brandRepository.saveAndFlush(BrandMapper.toDao(brand));
        return BrandMapper.toEntity(saved);
    }

    @Transactional(readOnly = true)
    public Page<Brand> getAllBrandsByFilter(FindAllBrandsFilter filter, Pageable pageable) {
        String searchPattern = Util.toSearchPattern(filter.getSearch());
        boolean filterActive = filter.getIsActive() != null;
        boolean activeValue = Boolean.TRUE.equals(filter.getIsActive());

        return brandRepository
                .findAllByFilters(searchPattern, filterActive, activeValue, pageable)
                .map(BrandMapper::toEntity);
    }

    @Transactional(readOnly = true)
    public Brand getBrandById(UUID id) {
        return BrandMapper.toEntity(findBrandDao(id));
    }

    @Transactional(readOnly = true)
    public java.util.List<Brand> getPublicBrands() {
        return brandRepository.findAllByIsActiveTrueOrderByPositionAsc()
                .stream()
                .map(BrandMapper::toEntity)
                .toList();
    }

    public Brand updateBrand(UUID id, UpdateBrandRequest request) {

        BrandDao brandDao = findBrandDao(id);

        String updatedTitle = request.getTitle() != null && !request.getTitle().isBlank()
                ? request.getTitle().trim()
                : brandDao.getTitle();
        Integer updatedPosition = request.getPosition() != null
                ? request.getPosition()
                : brandDao.getPosition();

        validatePosition(updatedPosition);

        if (!brandDao.getTitle().equalsIgnoreCase(updatedTitle)
                && brandRepository.existsByTitleIgnoreCaseAndIdNot(updatedTitle, id)) {
            throw new BrandTitleAlreadyExistsException(updatedTitle);
        }
        if (!updatedPosition.equals(brandDao.getPosition())
                && brandRepository.existsByPositionAndIdNot(updatedPosition, id)) {
            throw new DuplicateBrandPositionException(updatedPosition);
        }

        MultipartFile newLogo = request.getLogo();
        if (newLogo != null && !newLogo.isEmpty()) {
            ValidationUtil.validateMediaType(newLogo);
            requireActiveTransaction();

            String previousLogo = brandDao.getLogoUrl();
            String newFilename = fileStorageService.uploadPublicFile(newLogo, LOGO_DIRECTORY);
            brandDao.setLogoUrl(newFilename);
            registerLogoReplacementCleanup(previousLogo, newFilename);
        }

        brandDao.setTitle(updatedTitle);
        if (request.getDescription() != null) {
            brandDao.setDescription(request.getDescription());
        }
        brandDao.setPosition(updatedPosition);
        if (request.getIsActive() != null) {
            brandDao.setActive(request.getIsActive());
        }

        return BrandMapper.toEntity(brandRepository.saveAndFlush(brandDao));
    }

    public void deleteBrand(UUID id) {
        BrandDao brandDao = findBrandDao(id);
        String logo = brandDao.getLogoUrl();
        brandRepository.delete(brandDao);
        if (logo != null && !logo.isBlank()) {
            registerPostDeleteLogoCleanup(logo);
        }
    }

    public String getBrandLogoUrl(Brand brand) {
        if (brand.logoUrl() == null || brand.logoUrl().isBlank()) {
            return null;
        }
        return fileStorageService.getPresignedUrl(brand.logoUrl(), LOGO_URL_EXPIRY_MINUTES);
    }

    private BrandDao findBrandDao(UUID id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new BrandNotFoundException(id));
    }

    private void validatePosition(Integer position) {
        if (position == null) {
            throw new BadRequestException("Brand position is required.");
        }
        if (position <= 0) {
            throw new BadRequestException("Brand position must be a positive number.");
        }
    }

    private void requireActiveTransaction() {
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || !TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("Active transaction required");
        }
    }

    private void registerUploadedFileCleanup(String filename) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    safeDeleteFile(filename);
                }
            }
        });
    }

    private void registerLogoReplacementCleanup(String previousLogo, String newFilename) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                if (previousLogo == null || previousLogo.isBlank() || previousLogo.equals(newFilename)) {
                    return;
                }
                safeDeleteFile(previousLogo);
            }

            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    safeDeleteFile(newFilename);
                }
            }
        });
    }

    private void registerPostDeleteLogoCleanup(String logo) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                safeDeleteFile(logo);
            }
        });
    }

    private void safeDeleteFile(String filename) {
        try {
            fileStorageService.deleteFile(filename);
        } catch (Exception exception) {
            log.error("Failed to delete brand logo: {}", filename, exception);
        }
    }
}
