package com.familiemunshi.service;

import com.familiemunshi.common.utils.RoleNameUtil;
import com.familiemunshi.jpa.daos.RoleDao;
import com.familiemunshi.jpa.repositories.RoleRepository;
import com.familiemunshi.mapper.RoleMapper;
import com.familiemunshi.service.entities.Role;
import com.familiemunshi.service.enums.RoleScope;
import com.familiemunshi.service.exceptions.RoleAlreadyExistsException;
import com.familiemunshi.service.exceptions.RoleNotFoundException;
import com.familiemunshi.service.filters.FindAllRolesFilter;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;

    public Role createSystemRole(Role role) {
        Role newRole = role
                .withScope(RoleScope.PLATFORM)
                .withName(RoleNameUtil.platformRole(role.name()))
                .withIsSystem(false)
                .withIsActive(true);
        if(roleRepository.existsByNameIgnoreCaseAndScope(newRole.name(), newRole.scope())){
            throw new RoleAlreadyExistsException(newRole.name());
        }
        // MAP THE ROLE ENTITY TO DAO
        RoleDao roleMapper = RoleMapper.toDao(newRole);

        // SAVE THE ROLE DAO TO THE DATABASE
        RoleDao savedRole = roleRepository.save(roleMapper);

        return RoleMapper.toEntity(savedRole);
    }

    public Role getRoleById(UUID roleId) {
        return roleRepository.findById(roleId)
                .map(RoleMapper::toEntity)
                .orElseThrow(() -> new RoleNotFoundException(roleId));
    }

    public Page<Role> getAllRolesByFilter(
            FindAllRolesFilter filter,
            Pageable pageable
    ) {

        String search = filter.getSearch();

        if (search != null) {
            search = search.trim();

            if (search.isEmpty()) {
                search = null;
            }
        }

        Page<RoleDao> roles = roleRepository.findAllByFilters(
                RoleScope.PLATFORM,
                search,
                filter.getIsActive(),
                filter.getIsSystem(),
                pageable
        );

        return roles.map(RoleMapper::toEntity);
    }

}
