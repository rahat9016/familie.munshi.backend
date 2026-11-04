package com.familiemunshi.http.dtos.responses;

import com.familiemunshi.service.entities.CreatedBranchResult;

public record CreatedBranchResponse(
        BranchResponse branch,
        AdminResponse admin
) {

    public CreatedBranchResponse(
            CreatedBranchResult result,
            String logoUrl
    ) {
        this(
                new BranchResponse(
                        result.branch(),
                        logoUrl
                ),
                new AdminResponse(result.admin())
        );
    }
}