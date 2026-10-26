package com.familiemunshi.http.controllers;

import com.familiemunshi.http.dtos.responses.ModulePermissionTreeResponse;
import com.familiemunshi.http.dtos.responses.PermissionResponse;
import com.familiemunshi.http.dtos.responses.PermissionTreeItemResponse;
import com.familiemunshi.http.dtos.responses.SuccessResponse;
import com.familiemunshi.service.PermissionService;
import com.familiemunshi.service.entities.Permission;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.familiemunshi.common.utils.Constants.*;

@Tag(name = "Permission Management", description = "APIs for managing permissions")
@RequestMapping(API_BASE + PERMISSIONS)
@RestController
@AllArgsConstructor
public class PermissionController {
    private final PermissionService permissionService;

    @GetMapping
    public ResponseEntity<SuccessResponse<List<PermissionResponse>>> getAllPermissions(
            @RequestParam(required = false) String module
    ) {
        List<Permission> permissions =
                permissionService.getAllPermissions(module);

        List<PermissionResponse> response = permissions.stream()
                .map(PermissionResponse::new)
                .toList();

        return ResponseEntity.ok(
                SuccessResponse.of(
                        "Permissions retrieved successfully",
                        response
                )
        );
    }


    @GetMapping("/tree")
    public ResponseEntity<SuccessResponse<List<ModulePermissionTreeResponse>>>
    getPermissionTree() {

        List<ModulePermissionTreeResponse> response =
                permissionService.getPermissionTree()
                        .stream()
                        .map(group -> new ModulePermissionTreeResponse(
                                group.module(),
                                group.permissions()
                                        .stream()
                                        .map(PermissionTreeItemResponse::new)
                                        .toList()
                        ))
                        .toList();

        return ResponseEntity.ok(
                SuccessResponse.of(
                        "Permission tree retrieved successfully",
                        response
                )
        );
    }


    @GetMapping(ID)
    public ResponseEntity<SuccessResponse<PermissionResponse>> getPermissionById(
            @PathVariable("id") UUID id
    ) {
        Permission permission =
                permissionService.getPermissionById(id);

        return ResponseEntity.ok(
                SuccessResponse.of(
                        "Permission retrieved successfully",
                        new PermissionResponse(permission)
                )
        );
    }
}