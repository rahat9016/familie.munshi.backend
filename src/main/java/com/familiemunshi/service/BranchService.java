package com.familiemunshi.service;

import com.familiemunshi.common.service.FileStorageService;
import com.familiemunshi.http.dtos.requests.CreateBranchRequest;
import com.familiemunshi.jpa.daos.BranchDao;
import com.familiemunshi.jpa.repositories.BranchRepository;
import com.familiemunshi.service.exceptions.BranchNameAlreadyExistsException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class BranchService {
    private static final String BRANCHES_BUCKET = "branches";
    private final BranchRepository branchRepository;
    private final FileStorageService fileStorageService;

    public BranchDao createBranch(CreateBranchRequest branchRequest){
        // HANDLE NAME EXIST
        if(branchRepository.existsByName(branchRequest.getName())){
            throw new BranchNameAlreadyExistsException(branchRequest.getName());
        }
        BranchDao branchDao = branchRequest.toEntity();

        // GENERATE UNIQUE CODE
        String uniqueCode = generateUniqueBranchCode();
        branchDao.setCode(uniqueCode);

        // HANDLE FILE UPLOAD
        if(branchRequest.getLogo() != null && !branchRequest.getLogo().isEmpty()){
            final String filename = fileStorageService.uploadFile(branchRequest.getLogo());;
            branchDao.setLogoUrl(filename);
        }

        // SAVE BRANCH
        return branchRepository.save(branchDao);
    }


    private String generateUniqueBranchCode(){
        long nextId = branchRepository.count() + 1;
        String code;
        do{
            code = String.format("BR-%04d", nextId);
            nextId++;
        } while(branchRepository.existsByCode(code));
        return code;
    }
}
