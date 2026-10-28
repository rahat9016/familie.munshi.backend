package com.familiemunshi.service;

import com.familiemunshi.common.utils.Util;
import com.familiemunshi.jpa.daos.PermissionDao;
import com.familiemunshi.jpa.repositories.PermissionRepository;
import com.familiemunshi.mapper.PermissionMapper;
import com.familiemunshi.service.entities.Permission;
import com.familiemunshi.service.entities.PermissionGroup;
import com.familiemunshi.service.enums.PermissionCode;
import com.familiemunshi.service.exceptions.PermissionNotFoundException;
import com.familiemunshi.service.filters.FindAllPermissionsFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;


    @Transactional(readOnly = true)
    public Page<Permission> getAllPermissionsByFilter(
            FindAllPermissionsFilter filter,
            Pageable pageable
    ) {

        String searchPattern = Util.toSearchPattern(
                filter.getSearch()
        );

        String module = filter.getModule() == null
                ? null
                : filter.getModule().trim().toUpperCase(Locale.ROOT);

        List<String> permissionCodes = Arrays.stream(PermissionCode.values())
                .filter(permission ->
                        module == null
                                || module.isBlank()
                                || permission.getModule().equals(module)
                )
                .map(PermissionCode::getCode)
                .toList();

        if (permissionCodes.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid permission module: " + module
            );
        }

        Page<PermissionDao> permissions =
                permissionRepository.findAllByFilters(
                        permissionCodes,
                        searchPattern,
                        pageable
                );

        return permissions.map(PermissionMapper::toEntity);
    }


    @Transactional(readOnly = true)
    public List<PermissionGroup> getPermissionTree() {

        List<Permission> permissions = permissionRepository.findAll()
                .stream()
                .map(PermissionMapper::toEntity)
                .toList();

        Map<String, List<Permission>> groupedPermissions =
                permissions.stream()
                        .collect(Collectors.groupingBy(
                                permission -> permission.code().getModule(),
                                TreeMap::new,
                                Collectors.toList()
                        ));

        return groupedPermissions.entrySet()
                .stream()
                .map(entry -> new PermissionGroup(
                        entry.getKey(),
                        entry.getValue()
                                .stream()
                                .sorted(Comparator.comparing(
                                        permission -> permission.code().getCode()
                                ))
                                .toList()
                ))
                .toList();
    }


    @Transactional(readOnly = true)
    public Permission getPermissionById(UUID id) {
        PermissionDao permission = permissionRepository.findById(id)
                .orElseThrow(() -> new PermissionNotFoundException(id.toString()));

        return PermissionMapper.toEntity(permission);
    }
}
