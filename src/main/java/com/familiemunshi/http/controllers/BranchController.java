package com.familiemunshi.http.controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.familiemunshi.common.utils.Constants.*;

@Tag(name = "Branch Management", description = "APIs for managing business branches")
@RestController
@RequestMapping(API_BASE + BRANCHES)
public class BranchController {

    @PostMapping(consumes = "multipart/form-data")
    @Operation(
            summary = "Create a new branch.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Branch created."),
                    @ApiResponse(responseCode = "400", description = "Invalid input."),
                    @ApiResponse(responseCode = "401", description = "Unauthenticated.")
            }
    )
    public String createBranch() {
        return "Branch created successfully.";
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
