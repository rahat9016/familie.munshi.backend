package com.familiemunshi.http.controllers;


import com.familiemunshi.http.dtos.requests.CreateBranchRequest;
import com.familiemunshi.http.dtos.requests.UpdateBranchRequest;
import com.familiemunshi.http.dtos.responses.BranchResponse;
import com.familiemunshi.http.dtos.responses.CreatedBranchResponse;
import com.familiemunshi.http.dtos.responses.PaginatedResponse;
import com.familiemunshi.http.dtos.responses.SuccessResponse;
import com.familiemunshi.service.BranchService;
import com.familiemunshi.service.entities.Branch;
import com.familiemunshi.service.entities.CreatedBranchResult;
import com.familiemunshi.service.filters.FindAllBranchesFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.familiemunshi.common.utils.Constants.*;

@Tag(name = "Branch Management", description = "APIs for managing business branches")
@RestController
@RequestMapping(API_BASE + BRANCHES)
@RequiredArgsConstructor
public class BranchController {
    private final BranchService branchService;

    @PreAuthorize(
            "@permissionAuthorizationService.hasPermission(authentication, 'BRANCH_CREATE')"
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Create a new branch.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Branch created."),
                    @ApiResponse(responseCode = "400", description = "Invalid input."),
                    @ApiResponse(responseCode = "401", description = "Unauthenticated.")
            }
    )
    public ResponseEntity<SuccessResponse<CreatedBranchResponse>>
    createBranch(
            @Valid @ModelAttribute
            CreateBranchRequest branchRequest
    ) {

        // 1. CONVERT REQUEST TO ENTITY
        Branch branch = branchRequest.toEntity();

        // 2. CREATE BRANCH + ADMIN + ROLE + PERMISSIONS
        CreatedBranchResult createdResult =
                branchService.createBranch(
                        branch,
                        branchRequest.getLogo()
                );

        // 3. GENERATE PRESIGNED LOGO URL
        String logoUrl = getLogoUrlOrNull(
                createdResult.branch()
        );

        // 4. PREPARE RESPONSE
        CreatedBranchResponse response =
                new CreatedBranchResponse(
                        createdResult,
                        logoUrl
                );

        // 5. RETURN CREATED
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        SuccessResponse.of(
                                "Branch and Admin created successfully",
                                response
                        )
                );
    }



    @GetMapping
    @PreAuthorize("@permissionAuthorizationService.hasPermission(authentication, 'BRANCH_READ')")
    @Operation(
            summary = "Get all branches",
            description = "Search and filter branches with pagination.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Branches retrieved successfully"),
                    @ApiResponse(responseCode = "400",description = "Invalid pagination parameters"),
                    @ApiResponse( responseCode = "401", description = "Authentication required"),
                    @ApiResponse( responseCode = "403", description = "BRANCH_READ permission required")
            }
    )
    public ResponseEntity<PaginatedResponse<BranchResponse>> getAllBranches(

            @Valid @ParameterObject
            FindAllBranchesFilter filter,

            @Parameter(description = "Page number starting from 1",example = "1")
            @RequestParam(defaultValue = "1")
            @Min(1)
            int page,

            @Parameter(description = "Records per page, maximum 100",example = "20")
            @RequestParam(defaultValue = "20")
            @Min(1)
            @Max(100)
            int size
    ) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));

        // FETCH BRANCHES
        Page<Branch> branches = branchService.getAllBranchesByFilter(
                filter,
                pageable
        );

        // MAP TO RESPONSE WITH PRESIGNED LOGO URL
        List<BranchResponse> response = branches.getContent()
                .stream()
                .map(branch -> new BranchResponse(
                        branch,
                        branchService.getBranchLogoUrl(branch)
                ))
                .toList();

        return ResponseEntity.ok(
                PaginatedResponse.of(
                        "Branches retrieved successfully",
                        branches,
                        response
                )
        );
    }


    @PatchMapping(value = ID, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@permissionAuthorizationService.hasPermission(authentication, 'BRANCH_UPDATE')")
    @Operation(
            summary = "Update branch",
            description = "Partially update branch and replace its logo."
    )
    public ResponseEntity<SuccessResponse<BranchResponse>> updateBranch(
            @PathVariable("id") UUID branchId,
            @Valid @ModelAttribute UpdateBranchRequest request
    ) {
        Branch updatedBranch = branchService.updateBranch(branchId, request);
        String logoUrl = branchService.getBranchLogoUrl(updatedBranch);

        return ResponseEntity.ok(
                SuccessResponse.of(
                        "Branch updated successfully",
                        new BranchResponse(updatedBranch, logoUrl)
                )
        );
    }


    @GetMapping(ID)
    @PreAuthorize("@permissionAuthorizationService.hasPermission(authentication, 'BRANCH_READ')")
    @Operation(
            summary = "Get branch by ID", description = "Retrieve branch details with MinIO presigned logo URL.",
            responses = {
                @ApiResponse(responseCode = "200", description = "Branch retrieved successfully"),
                @ApiResponse(responseCode = "401", description = "Unauthenticated"),
                @ApiResponse(responseCode = "403", description = "Permission denied"),
                @ApiResponse(responseCode = "404", description = "Branch not found")
            }
    )
    public ResponseEntity<SuccessResponse<BranchResponse>> getBranchById(@PathVariable("id") UUID branchId) {
        Branch branch = branchService.getBranchById(branchId);
        String logoUrl = branchService.getBranchLogoUrl(branch);
        return ResponseEntity.ok(SuccessResponse.of("Branch retrieved successfully", new BranchResponse(branch, logoUrl)));
    }

    private String getLogoUrlOrNull(Branch branch) {

        if (branch.logoUrl() == null
                || branch.logoUrl().isBlank()) {
            return null;
        }

        try {
            return branchService.getBranchLogoUrl(branch);
        } catch (Exception exception) {
            return null;
        }
    }

}
