package com.familiemunshi.service;

import com.familiemunshi.common.service.FileStorageService;
import com.familiemunshi.http.validations.ValidationUtil;
import com.familiemunshi.jpa.daos.BranchDao;
import com.familiemunshi.jpa.repositories.BranchRepository;
import com.familiemunshi.mapper.BranchMapper;
import com.familiemunshi.service.entities.Branch;
import com.familiemunshi.service.exceptions.BranchNameAlreadyExistsException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class BranchService {
    private static final int LOGO_URL_EXPIRY_MINUTES = 7 * 24 * 60;
    private final BranchRepository branchRepository;
    private final FileStorageService fileStorageService;

    public Branch createBranch(Branch branch, MultipartFile logoFile) {
        // HANDLE NAME EXIST
        if (branchRepository.existsByName(branch.name())) {
            throw new BranchNameAlreadyExistsException(branch.name());
        }

        // GENERATE UNIQUE CODE
        String uniqueCode = generateUniqueBranchCode();
        branch = branch.withCode(uniqueCode);

        // HANDLE FILE UPLOAD
        if (logoFile != null && !logoFile.isEmpty()) {
            ValidationUtil.validateMediaType(logoFile);
            final String filename = fileStorageService.uploadFile(logoFile);
            branch = branch.withLogoUrl(filename);
        }

        // MAP TO DAO
        BranchDao branchDao = BranchMapper.toDao(branch);

        // SAVE BRANCH
        BranchDao savedBranch = branchRepository.save(branchDao);

        // MAP BACK TO ENTITY
        return BranchMapper.toEntity(savedBranch);
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

}
