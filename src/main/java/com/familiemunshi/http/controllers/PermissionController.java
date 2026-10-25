package com.familiemunshi.http.controllers;

import com.familiemunshi.http.dtos.requests.PermissionRequest;
import com.familiemunshi.service.entities.Permission;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.familiemunshi.common.utils.Constants.*;

@Tag(name = "Permission Management", description = "APIs for managing permissions")
@RequestMapping(API_BASE + PERMISSIONS)
@RestController
public class PermissionController {
    @PostMapping()
    public String createPermission(@Valid @RequestBody PermissionRequest permissionRequest) {
        Permission permission = permissionRequest.toEntity();
        return "Permission created.";
    }
}