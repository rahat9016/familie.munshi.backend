package com.familiemunshi.service;

import com.familiemunshi.http.dtos.responses.MyPermissionsResponse;
import com.familiemunshi.jpa.repositories.PermissionRepository;
import com.familiemunshi.jpa.repositories.UserBranchRoleRepository;
import com.familiemunshi.jpa.repositories.UserPlatformRoleRepository;
import com.familiemunshi.jpa.repositories.UserRepository;
import com.familiemunshi.service.enums.RoleScope;
import com.familiemunshi.service.exceptions.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserPlatformRoleRepository userPlatformRoleRepository;

    @Mock
    private UserBranchRoleRepository userBranchRoleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private PermissionService permissionService;

    @Test
    void getMyPermissions_userWithoutAnyRole_returnsEmptyCustomerPermissions() {
        UUID userId = UUID.randomUUID();

        when(userRepository.existsByIdAndIsActiveTrue(userId)).thenReturn(true);
        when(userPlatformRoleRepository.findEffectiveRoleNames(userId, RoleScope.PLATFORM))
                .thenReturn(List.of());
        when(userBranchRoleRepository.findAuthorizedBranches(userId, RoleScope.BRANCH))
                .thenReturn(List.of());

        MyPermissionsResponse response = permissionService.getMyPermissions(userId);

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.scope()).isNull();
        assertThat(response.roles()).isEmpty();
        assertThat(response.permissions()).isEmpty();
        assertThat(response.branches()).isNull();
        assertThat(response.branchId()).isNull();
    }

    @Test
    void getMyPermissions_inactiveOrMissingUser_throwsUserNotFound() {
        UUID userId = UUID.randomUUID();

        when(userRepository.existsByIdAndIsActiveTrue(userId)).thenReturn(false);

        assertThatThrownBy(() -> permissionService.getMyPermissions(userId))
                .isInstanceOf(UserNotFoundException.class);

        verifyNoInteractions(userPlatformRoleRepository, userBranchRoleRepository);
    }
}
