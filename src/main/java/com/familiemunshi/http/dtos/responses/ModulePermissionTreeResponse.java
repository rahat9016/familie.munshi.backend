package com.familiemunshi.http.dtos.responses;

import java.util.List;

public record ModulePermissionTreeResponse(
        String module,
        List<PermissionTreeItemResponse> permissions
) {}
