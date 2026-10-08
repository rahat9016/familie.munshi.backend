package com.familiemunshi.service;

import com.familiemunshi.http.dtos.requests.CreateBranchRequest;
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

    public void createBranch(CreateBranchRequest branchRequest){
        // HANDLE CODE EXIST
        if(branchRepository.existsByCode(branchRequest.getCode())){
            throw new IllegalArgumentException("Branch code already exists");
        }
        // HANDLE NAME EXIST
        // HANDLE FILE UPLOAD

    }
}
