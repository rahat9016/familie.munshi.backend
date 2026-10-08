package com.familiemunshi.service;

import com.familiemunshi.common.service.FileStorageService;
import com.familiemunshi.http.dtos.requests.CreateBranchRequest;
import com.familiemunshi.jpa.daos.BranchDao;
import com.familiemunshi.jpa.repositories.BranchRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class BranchService {
    private static final String BRANCHES_BUCKET = "branches";

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private FileStorageService fileStorageService;

    public BranchDao createBranch(CreateBranchRequest branchRequest){
        // HANDLE CODE EXIST
        if(branchRepository.existsByCode(branchRequest.getCode())){
            throw new IllegalArgumentException("Branch code already exists");
        }
        // HANDLE NAME EXIST
        if(branchRepository.existsByName(branchRequest.getName())){
            throw new IllegalArgumentException("Branch name already exists");
        }

        BranchDao branchDao = branchRequest.toEntity();

        // HANDLE FILE UPLOAD
        if(branchRequest.getLogo() != null && !branchRequest.getLogo().isEmpty()){
            String filename = fileStorageService.uploadFile(branchRequest.getLogo());;
            branchDao.setLogoUrl(filename);
        }

        // SAVE BRANCH
        return branchRepository.save(branchDao);
    }
}
