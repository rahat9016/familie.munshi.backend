package com.familiemunshi.service.entities;

public record CreatedBranchResult(
        Branch branch,
        BranchAdminCredentials admin
) {}
