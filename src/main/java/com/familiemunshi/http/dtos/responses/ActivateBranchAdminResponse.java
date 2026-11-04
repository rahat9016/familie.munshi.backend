
package com.familiemunshi.http.dtos.responses;

public record ActivateBranchAdminResponse(
        String email,
        boolean active
) {
}
