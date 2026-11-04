package com.familiemunshi.http.dtos.responses;

import com.familiemunshi.service.entities.BranchAdminCredentials;

import java.util.UUID;

public record AdminResponse(
        UUID userId,
        UUID roleId,
        String email,
        String role,
        String status
) {

    public AdminResponse(BranchAdminCredentials admin) {
        this(
                admin.userId(),
                admin.roleId(),
                admin.email(),
                admin.role(),
                admin.active()
                        ? "ACTIVE"
                        : "PENDING_ACTIVATION"
        );
    }
}
