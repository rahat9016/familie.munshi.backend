package com.familiemunshi.http.controllers;


import com.familiemunshi.http.dtos.requests.CreateBranchRequest;
import com.familiemunshi.jpa.daos.BranchDao;
import com.familiemunshi.service.BranchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.familiemunshi.common.utils.Constants.*;

@Tag(name = "Branch Management", description = "APIs for managing business branches")
@RestController
@RequestMapping(API_BASE + BRANCHES)
@RequiredArgsConstructor
public class BranchController {
    private final BranchService branchService;
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Create a new branch.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Branch created."),
                    @ApiResponse(responseCode = "400", description = "Invalid input."),
                    @ApiResponse(responseCode = "401", description = "Unauthenticated.")
            }
    )
    public ResponseEntity<BranchDao> createBranch(@Valid @ModelAttribute CreateBranchRequest branchRequest) {
        BranchDao branchDao = branchService.createBranch(branchRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(branchDao);
    }

    @GetMapping
    @Operation(
            summary = "Get all branches.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Branches retrieved."),
                    @ApiResponse(responseCode = "401", description = "Unauthenticated.")
            }
    )
    public String getAllBranches() {
        return "List of all branches.";
    }

}
