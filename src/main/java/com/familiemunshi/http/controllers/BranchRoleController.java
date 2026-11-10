package com.familiemunshi.http.controllers;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.familiemunshi.common.utils.Constants.*;



@Tag(name = "Branch Roles", description = "APIs for managing branch roles")
@RestController
@RequestMapping(API_BASE + BRANCHES + BRANCH_ID + ROLES)
@AllArgsConstructor
public class BranchRoleController {
    @Schema(description = "Create a new role for a branch", requiredMode = Schema.RequiredMode.REQUIRED)
    @PostMapping(consumes = "application/json")
    public void createBranchRole(){}
}
