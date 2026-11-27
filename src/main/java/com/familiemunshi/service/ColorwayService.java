package com.familiemunshi.service;

import com.familiemunshi.common.component.StorageFileSupport;
import com.familiemunshi.common.exceptions.BadRequestException;
import com.familiemunshi.common.service.FileStorageService;
import com.familiemunshi.common.utils.Util;
import com.familiemunshi.http.dtos.requests.UpdateColorwayRequest;
import com.familiemunshi.http.dtos.responses.ColorwayResponse;
import com.familiemunshi.http.validations.ValidationUtil;
import com.familiemunshi.jpa.daos.ColorwayDao;
import com.familiemunshi.jpa.daos.ColorwayImageDao;
import com.familiemunshi.jpa.repositories.ColorwayRepository;
import com.familiemunshi.mapper.ColorwayMapper;
import com.familiemunshi.service.entities.Colorway;
import com.familiemunshi.service.exceptions.ColorwayImageNotFoundException;
import com.familiemunshi.service.exceptions.ColorwayNotFoundException;
import com.familiemunshi.service.filters.FindAllColorwaysFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ColorwayService {

    public static final int MAX_IMAGES = 10;

    private static final int IMAGE_URL_EXPIRY_MINUTES = 7 * 24 * 60;
    private static final String IMAGE_DIRECTORY = StorageFileSupport.COLORWAY_IMAGES;
    private static final String CODE_PREFIX = "CLR-";
    private static final String CODE_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;
    private static final int CODE_MAX_ATTEMPTS = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ColorwayRepository colorwayRepository;
    private final FileStorageService fileStorageService;

    // ------------------------------------------------------------------ CRUD

    public Colorway createColorway(Colorway colorway) {
        ColorwayDao dao = ColorwayMapper.toDao(colorway.withCode(generateUniqueCode()));
        return ColorwayMapper.toEntity(colorwayRepository.saveAndFlush(dao));
    }

    @Transactional(readOnly = true)
    public Page<Colorway> getAllColorwaysByFilter(FindAllColorwaysFilter filter, Pageable pageable) {
        String searchPattern = Util.toSearchPattern(filter.getSearch());
        // codes stored uppercase -> match without LOWER() so the pattern index is usable
        String codePattern = Util.toSearchPattern(filter.getCode()).toUpperCase(Locale.ROOT);
        String colorStandard = normalizeStandard(filter.getColorStandard());

        return colorwayRepository.findAllByFilters(
                        searchPattern,
                        codePattern,
                        colorStandard == null ? "" : colorStandard,
                        filter.getIsActive() != null, Boolean.TRUE.equals(filter.getIsActive()),
                        filter.getInTheme() != null, Boolean.TRUE.equals(filter.getInTheme()),
                        pageable)
                .map(ColorwayMapper::toEntity);
    }

    @Transactional(readOnly = true)
    public Colorway getColorwayById(UUID id) {
        return ColorwayMapper.toEntity(findColorwayDao(id));
    }

    public Colorway updateColorway(UUID id, UpdateColorwayRequest r) {
        ColorwayDao dao = findColorwayDao(id);

        LocalDate start = r.getStartDate() != null ? r.getStartDate() : dao.getStartDate();
        LocalDate end = r.getEndDate() != null ? r.getEndDate() : dao.getEndDate();
        LocalDate clearance = r.getClearanceDate() != null ? r.getClearanceDate() : dao.getClearanceDate();
        validateDates(start, end, clearance);

        if (hasText(r.getMarketingName())) dao.setMarketingName(r.getMarketingName().trim());
        if (hasText(r.getName())) dao.setName(r.getName().trim());
        if (r.getColorSpecification() != null) dao.setColorSpecification(r.getColorSpecification());
        if (r.getDescription() != null) dao.setDescription(r.getDescription());

        String standard = normalizeStandard(r.getColorStandard());
        if (standard != null) dao.setColorStandard(standard);
        if (r.getColorReference() != null) {
            dao.setColorReference(hasText(r.getColorReference()) ? r.getColorReference().trim() : null);
        }
        if (r.getHexCode() != null) dao.setHexCode(r.getHexCode().toUpperCase(Locale.ROOT));

        if (r.getIsActive() != null) dao.setActive(r.getIsActive());
        if (r.getInTheme() != null) dao.setInTheme(r.getInTheme());
        if (r.getSustainabilityLabel() != null) dao.setSustainabilityLabel(r.getSustainabilityLabel());
        if (r.getPlanSms() != null) dao.setPlanSms(r.getPlanSms());
        if (r.getPlan3dSms() != null) dao.setPlan3dSms(r.getPlan3dSms());
        if (r.getActualSms() != null) dao.setActualSms(r.getActualSms());

        dao.setStartDate(start);
        dao.setEndDate(end);
        dao.setClearanceDate(clearance);

        return ColorwayMapper.toEntity(colorwayRepository.saveAndFlush(dao));
    }

    public void deleteColorway(UUID id) {
        ColorwayDao dao = findColorwayDao(id);
        List<String> keys = dao.getImages().stream().map(ColorwayImageDao::getImageKey).toList();
        colorwayRepository.delete(dao);
        registerDeleteAfterCommit(keys);
    }

    // ---------------------------------------------------------------- Images

    public Colorway addImages(UUID id, List<MultipartFile> imageFiles) {
        ColorwayDao dao = findColorwayDao(id);

        List<MultipartFile> files = nonEmpty(imageFiles);
        if (files.isEmpty()) {
            throw new BadRequestException("At least one image is required.");
        }
        validateImages(files, dao.getImages().size());

        uploadAndAttach(dao, files);
        return ColorwayMapper.toEntity(colorwayRepository.saveAndFlush(dao));
    }

    public Colorway deleteImage(UUID id, UUID imageId) {
        ColorwayDao dao = findColorwayDao(id);

        ColorwayImageDao image = dao.getImages().stream()
                .filter(i -> i.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new ColorwayImageNotFoundException(imageId));

        dao.getImages().remove(image);
        renumber(dao.getImages());
        registerDeleteAfterCommit(List.of(image.getImageKey()));

        return ColorwayMapper.toEntity(colorwayRepository.saveAndFlush(dao));
    }

    public Colorway reorderImages(UUID id, List<UUID> orderedIds) {
        ColorwayDao dao = findColorwayDao(id);

        Map<UUID, ColorwayImageDao> byId = new HashMap<>();
        dao.getImages().forEach(i -> byId.put(i.getId(), i));

        if (orderedIds.size() != byId.size() || !byId.keySet().equals(new HashSet<>(orderedIds))) {
            throw new BadRequestException("Image order must contain every image of the colorway exactly once.");
        }

        for (int i = 0; i < orderedIds.size(); i++) {
            byId.get(orderedIds.get(i)).setPosition(i);
        }
        dao.getImages().sort(Comparator.comparing(ColorwayImageDao::getPosition));

        return ColorwayMapper.toEntity(colorwayRepository.saveAndFlush(dao));
    }

    public List<ColorwayResponse.ImageResponse> resolveImages(Colorway colorway) {
        return colorway.images().stream()
                .map(img -> new ColorwayResponse.ImageResponse(
                        img.id(),
                        fileStorageService.getPresignedUrl(img.imageKey(), IMAGE_URL_EXPIRY_MINUTES),
                        img.position()))
                .toList();
    }

    // --------------------------------------------------------------- Helpers

    private ColorwayDao findColorwayDao(UUID id) {
        return colorwayRepository.findById(id)
                .orElseThrow(() -> new ColorwayNotFoundException(id));
    }

    private String generateUniqueCode() {
        for (int attempt = 0; attempt < CODE_MAX_ATTEMPTS; attempt++) {
            StringBuilder sb = new StringBuilder(CODE_PREFIX);
            for (int i = 0; i < CODE_LENGTH; i++) {
                sb.append(CODE_ALPHABET.charAt(RANDOM.nextInt(CODE_ALPHABET.length())));
            }
            String code = sb.toString();
            if (!colorwayRepository.existsByCode(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Unable to generate unique colorway code");
    }

    private void validateDates(LocalDate start, LocalDate end, LocalDate clearance) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new BadRequestException("End date cannot be before start date.");
        }
        if (end != null && clearance != null && clearance.isBefore(end)) {
            throw new BadRequestException("Clearance date cannot be before end date.");
        }
    }

    private void validateImages(List<MultipartFile> files, int existingCount) {
        if (existingCount + files.size() > MAX_IMAGES) {
            throw new BadRequestException("A colorway can have at most " + MAX_IMAGES + " images.");
        }
        files.forEach(ValidationUtil::validateMediaType);
    }

    private void uploadAndAttach(ColorwayDao dao, List<MultipartFile> files) {
        requireActiveTransaction();

        int nextPosition = dao.getImages().size();
        List<String> uploaded = new ArrayList<>();
        // registered before uploading so a mid-loop failure still removes earlier uploads
        registerDeleteOnRollback(uploaded);

        for (MultipartFile file : files) {
            String key = fileStorageService.uploadPublicFile(file, IMAGE_DIRECTORY);
            uploaded.add(key);
            dao.addImage(ColorwayImageDao.builder()
                    .imageKey(key)
                    .position(nextPosition++)
                    .build());
        }
    }

    private void renumber(List<ColorwayImageDao> images) {
        images.sort(Comparator.comparing(ColorwayImageDao::getPosition));
        for (int i = 0; i < images.size(); i++) {
            images.get(i).setPosition(i);
        }
    }

    private List<MultipartFile> nonEmpty(List<MultipartFile> files) {
        if (files == null) return List.of();
        return files.stream().filter(f -> f != null && !f.isEmpty()).toList();
    }

    private String normalizeStandard(String value) {
        return hasText(value) ? value.trim().toUpperCase(Locale.ROOT) : null;
    }

    private boolean hasText(String s) {
        return s != null && !s.isBlank();
    }

    private void requireActiveTransaction() {
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || !TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("Active transaction required");
        }
    }

    private void registerDeleteOnRollback(List<String> keys) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    keys.forEach(ColorwayService.this::safeDeleteFile);
                }
            }
        });
    }

    private void registerDeleteAfterCommit(List<String> keys) {
        if (keys.isEmpty()) return;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                keys.forEach(ColorwayService.this::safeDeleteFile);
            }
        });
    }

    private void safeDeleteFile(String key) {
        try {
            fileStorageService.deleteFile(key);
        } catch (Exception e) {
            log.error("Failed to delete colorway image: {}", key, e);
        }
    }
}
