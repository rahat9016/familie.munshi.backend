
package com.familiemunshi.http.dtos.responses;

import java.util.List;
import java.util.UUID;

public record BranchPermissionResponse(
        UUID branchId,
        String branchCode,
        String branchName,
        List<String> roles,
        List<String> permissions
) {
}
