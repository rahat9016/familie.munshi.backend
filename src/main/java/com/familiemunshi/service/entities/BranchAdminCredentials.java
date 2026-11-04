package com.familiemunshi.service.entities;

import java.util.UUID;

public record BranchAdminCredentials(
        UUID userId,
        UUID roleId,
        String email,
        String role,
        boolean active
) {}
