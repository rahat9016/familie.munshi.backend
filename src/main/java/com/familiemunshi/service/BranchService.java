package com.familiemunshi.service;

import com.familiemunshi.common.service.FileStorageService;
import com.familiemunshi.common.utils.Util;
import com.familiemunshi.http.dtos.requests.UpdateBranchRequest;
import com.familiemunshi.http.validations.ValidationUtil;
import com.familiemunshi.jpa.daos.BranchDao;
import com.familiemunshi.jpa.repositories.BranchRepository;
import com.familiemunshi.mapper.BranchMapper;
import com.familiemunshi.service.entities.Branch;
import com.familiemunshi.service.entities.CreatedBranchResult;
import com.familiemunshi.service.exceptions.BranchNameAlreadyExistsException;
import com.familiemunshi.service.exceptions.BranchNotFoundException;
import com.familiemunshi.service.filters.FindAllBranchesFilter;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BranchService {
    private static final int LOGO_URL_EXPIRY_MINUTES = 7 * 24 * 60;
    private final BranchRepository branchRepository;
    private final FileStorageService fileStorageService;
    private final BranchAdminProvisioner branchAdminProvisioner;

    // CREATE BRANCH
    public CreatedBranchResult createBranch(
            Branch branch,
            MultipartFile logoFile
    ) {

        if (branchRepository.existsByName(branch.name())) {
            throw new BranchNameAlreadyExistsException(
                    branch.name()
            );
        }

        String branchCode = generateUniqueBranchCode();

        branch = branch
                .withCode(branchCode);

        // OPTIONAL LOGO
        if (logoFile != null && !logoFile.isEmpty()) {

            ValidationUtil.validateMediaType(logoFile);
            requireActiveTransaction();

            String filename =
                    fileStorageService.uploadFile(logoFile);

            registerUploadedFileCleanup(filename);

            branch = branch.withLogoUrl(filename);
        }

        // SAVE BRANCH
        BranchDao branchDao = BranchMapper.toDao(branch);

        BranchDao savedBranch =
                branchRepository.saveAndFlush(branchDao);

        // CREATE ADMIN + ROLE + ASSIGNMENTS
        var admin = branchAdminProvisioner.provision(
                savedBranch
        );

        return new CreatedBranchResult(
                BranchMapper.toEntity(savedBranch),
                admin
        );
    }


    // GET ALL BRANCHES WITH FILTERS
    public Page<Branch> getAllBranchesByFilter(FindAllBranchesFilter filter, Pageable pageable) {

        // NORMALIZE SEARCH
        String searchPattern = Util.toSearchPattern(filter.getSearch());

        // OPTIONAL ACTIVE FILTER
        boolean filterActive = filter.getIsActive() != null;

        boolean activeValue = Boolean.TRUE.equals(filter.getIsActive());

        // GET PAGINATED DATA
        Page<BranchDao> branches = branchRepository.findAllByFilters(searchPattern, filterActive, activeValue, pageable);

        // DAO -> DOMAIN
        return branches.map(BranchMapper::toEntity);
    }

    // UPDATE BRANCH
    public Branch updateBranch(UUID branchId, UpdateBranchRequest request) {

        // 1. FIND EXISTING BRANCH
        BranchDao branchDao = branchRepository.findById(branchId).orElseThrow(() -> new BranchNotFoundException(branchId));

        // 2. CONVERT DAO TO DOMAIN
        Branch existingBranch = BranchMapper.toEntity(branchDao);

        // 3. VALIDATE AND APPLY PARTIAL UPDATE
        Branch updatedBranch = request.toEntity(existingBranch);

        // 4. VALIDATE UNIQUE NAME
        if (!existingBranch.name().equals(updatedBranch.name()) && branchRepository.existsByNameAndIdNot(updatedBranch.name(), branchId)) {
            throw new BranchNameAlreadyExistsException(updatedBranch.name());
        }

        // 5. HANDLE LOGO REPLACEMENT
        MultipartFile newLogo = request.getLogo();

        if (newLogo != null) {
            String previousLogo = branchDao.getLogoUrl();

            // VERIFY TRANSACTION SYNCHRONIZATION
            if (!TransactionSynchronizationManager.isSynchronizationActive()) {
                throw new IllegalStateException("Active transaction synchronization is required");
            }

            // UPLOAD NEW IMAGE FIRST
            String newFilename = fileStorageService.uploadFile(newLogo);

            // UPDATE DOMAIN WITH NEW OBJECT KEY
            updatedBranch = updatedBranch.withLogoUrl(newFilename);

            // REGISTER CLEANUP CALLBACKS
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    // DELETE OLD LOGO ONLY AFTER DB COMMIT
                    if (previousLogo == null || previousLogo.isBlank() || previousLogo.equals(newFilename)) return;
                    try {
                        fileStorageService.deleteFile(previousLogo);
                        log.info("Previous branch logo deleted: {}", previousLogo);
                    } catch (Exception exception) {
                        // DB IS ALREADY COMMITTED.
                        // DO NOT FAIL THE API RESPONSE.
                        log.error("Failed to delete previous branch logo: {}", previousLogo, exception);
                    }
                }

                @Override
                public void afterCompletion(int status) {

                    // ROLLBACK: DELETE NEWLY UPLOADED FILE
                    if (status == STATUS_COMMITTED) {
                        return;
                    }

                    try {
                        fileStorageService.deleteFile(newFilename);
                        log.info("Rolled-back branch logo deleted: {}", newFilename);
                    } catch (Exception exception) {
                        log.error("Failed to clean up uploaded logo: {}", newFilename, exception);
                    }
                }
            });
        }

        // 6. APPLY UPDATED FIELDS TO MANAGED DAO
        branchDao.setName(updatedBranch.name());
        branchDao.setAddress(updatedBranch.address());
        branchDao.setPhone(updatedBranch.phone());
        branchDao.setActive(updatedBranch.isActive());
        branchDao.setLogoUrl(updatedBranch.logoUrl());

        // 7. SAVE DATABASE CHANGES
        BranchDao savedBranch = branchRepository.saveAndFlush(branchDao);

        // 8. RETURN DOMAIN
        return BranchMapper.toEntity(savedBranch);
    }


    // GET BRANCH BY ID
    public Branch getBranchById(UUID branchId) {
        BranchDao branchDao = branchRepository.findById(branchId).orElseThrow(() -> new BranchNotFoundException(branchId));
        return BranchMapper.toEntity(branchDao);
    }


    private String generateUniqueBranchCode() {
        long nextId = branchRepository.count() + 1;
        String code;
        do {
            code = String.format("BR-%04d", nextId);
            nextId++;
        } while (branchRepository.existsByCode(code));
        return code;
    }



    public String getBranchLogoUrl(Branch branch) {

        if (branch.logoUrl() == null
                || branch.logoUrl().isBlank()) {
            return null;
        }

        return fileStorageService.getPresignedUrl(
                branch.logoUrl(),
                LOGO_URL_EXPIRY_MINUTES
        );
    }

    private void requireActiveTransaction() {

        if (!TransactionSynchronizationManager
                .isActualTransactionActive()
                || !TransactionSynchronizationManager
                .isSynchronizationActive()) {

            throw new IllegalStateException(
                    "Active transaction required"
            );
        }
    }


    private void registerUploadedFileCleanup(
            String filename
    ) {

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {

                    @Override
                    public void afterCompletion(int status) {

                        if (status != STATUS_COMMITTED) {
                            safeDeleteFile(filename);
                        }
                    }
                }
        );
    }
    private void safeDeleteFile(String filename) {

        try {
            fileStorageService.deleteFile(filename);
        } catch (Exception exception) {

            log.error(
                    "Failed to delete Branch logo: {}",
                    filename,
                    exception
            );
        }
    }
}
