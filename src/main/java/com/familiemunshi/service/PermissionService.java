package com.familiemunshi.service;

import com.familiemunshi.jpa.daos.PermissionDao;
import com.familiemunshi.jpa.repositories.PermissionRepository;
import com.familiemunshi.mapper.PermissionMapper;
import com.familiemunshi.service.entities.Permission;
import com.familiemunshi.service.entities.PermissionGroup;
import com.familiemunshi.service.exceptions.PermissionNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;

    @Transactional(readOnly = true)
    public List<Permission> getAllPermissions(String module) {

        return permissionRepository.findAll()
                .stream()
                .map(PermissionMapper::toEntity)
                .filter(permission ->
                        module == null
                                || module.isBlank()
                                || permission.code().getModule().equals(
                                module.trim().toUpperCase(Locale.ROOT)
                        )
                )
                .sorted((a, b) ->
                        a.code().getCode().compareTo(
                                b.code().getCode()
                        )
                )
                .toList();
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
