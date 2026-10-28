package com.familiemunshi.http.controllers;

import com.familiemunshi.http.dtos.responses.*;
import com.familiemunshi.service.PermissionService;
import com.familiemunshi.service.entities.Permission;
import com.familiemunshi.service.filters.FindAllPermissionsFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

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
    @PreAuthorize(
            "@permissionAuthorizationService.hasPermission(authentication, 'PERMISSION_READ')"
    )
    @Operation(
            summary = "Get all permissions",
            description = "Search permissions and filter by module with pagination.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Permissions retrieved successfully"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid filter or pagination parameters"
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Authentication required"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "PERMISSION_READ permission required"
                    )
            }
    )
    public ResponseEntity<PaginatedResponse<PermissionResponse>>
    getAllPermissions(
            @Valid @ParameterObject
            FindAllPermissionsFilter filter,

            @Parameter(description = "Page number starting from 1", example = "1")
            @RequestParam(defaultValue = "1")
            @Min(1)
            int page,

            @Parameter(description = "Records per page (maximum 100)", example = "20")
            @RequestParam(defaultValue = "20")
            @Min(1)
            @Max(100)
            int size
    ) {

        Pageable pageable = PageRequest.of(
                page - 1,
                size,
                Sort.by(Sort.Order.asc("name"), Sort.Order.asc("id"))
        );

        Page<Permission> permissions =
                permissionService.getAllPermissionsByFilter(
                        filter,
                        pageable
                );

        List<PermissionResponse> response = permissions
                .getContent()
                .stream()
                .map(PermissionResponse::new)
                .toList();

        return ResponseEntity.ok(
                PaginatedResponse.of(
                        "Permissions retrieved successfully",
                        permissions,
                        response
                )
        );
    }


    @PreAuthorize(
            "@permissionAuthorizationService.hasPermission(authentication, 'PERMISSION_READ')"
    )
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

    @PreAuthorize(
            "@permissionAuthorizationService.hasPermission(authentication, 'PERMISSION_READ')"
    )
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